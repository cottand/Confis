package eu.dcotta.confis.scripting

import kotlin.script.experimental.api.CompiledScript
import kotlin.script.experimental.api.ScriptCompilationConfiguration
import kotlin.script.experimental.api.SourceCode
import kotlin.script.experimental.jvm.CompiledJvmScriptsCache
import kotlin.script.experimental.jvmhost.CompiledScriptJarsCache
import java.io.File
import java.security.MessageDigest

class ConfisFileSystemCache(dir: File) : CompiledJvmScriptsCache by CompiledScriptJarsCache({ source, config ->
    File(dir, compiledScriptUniqueName(source, config) + ".jar")
})

class InMemoryCache(private val maxSize: Int = 20) : CompiledJvmScriptsCache {
    private val map = LinkedHashMap<String, CompiledScript>(maxSize, 0.7f, true)

    override fun get(
        script: SourceCode,
        scriptCompilationConfiguration: ScriptCompilationConfiguration,
    ) = map[compiledScriptUniqueName(script, scriptCompilationConfiguration)]

    override fun store(
        compiledScript: CompiledScript,
        script: SourceCode,
        scriptCompilationConfiguration: ScriptCompilationConfiguration,
    ) {
        if (map.size >= maxSize) map.remove(map.entries.first().key)
        map[compiledScriptUniqueName(script, scriptCompilationConfiguration)] = compiledScript
    }

    fun dispose() = map.clear()
}

/**
 * 2-level cache for scripts
 */
class InMemAndFileSystemCache(dir: File, memMaxSize: Int = 20) : CompiledJvmScriptsCache {
    private val mem = InMemoryCache(memMaxSize)
    private val fs = ConfisFileSystemCache(dir)

    override fun get(
        script: SourceCode,
        scriptCompilationConfiguration: ScriptCompilationConfiguration,
    ): CompiledScript? =
        mem.get(script, scriptCompilationConfiguration)
            // if found in filesystem but not memory, also store in mem while we're fetching
            // to keep the script warm
            ?: fs.get(script, scriptCompilationConfiguration)?.also { compiled ->
                mem.store(compiled, script, scriptCompilationConfiguration)
            }

    override fun store(
        compiledScript: CompiledScript,
        script: SourceCode,
        scriptCompilationConfiguration: ScriptCompilationConfiguration,
    ) {
        mem.store(compiledScript, script, scriptCompilationConfiguration)
        fs.store(compiledScript, script, scriptCompilationConfiguration)
    }

    fun dispose() = mem.dispose()
}

private fun ByteArray.toHex(): String = joinToString(separator = "") { byte -> "%02x".format(byte) }

private fun compiledScriptUniqueName(
    script: SourceCode,
    scriptCompilationConfiguration: ScriptCompilationConfiguration,
): String =
    MessageDigest.getInstance("MD5")
        .apply {
            update(script.text.toByteArray())
            scriptCompilationConfiguration.notTransientData.entries
                .sortedBy { it.key.name }
                .forEach {
                    update(it.key.name.toByteArray())
                    update(it.value.toString().toByteArray())
                }
        }.digest().toHex()
