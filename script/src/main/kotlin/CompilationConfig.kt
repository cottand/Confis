package eu.dcotta.confis.scripting

import eu.dcotta.confis.dsl.AgreementBuilder
import eu.dcotta.confis.model.Month
import eu.dcotta.confis.model.Purpose
import kotlin.script.experimental.api.ScriptAcceptedLocation.Everywhere
import kotlin.script.experimental.api.ScriptCompilationConfiguration
import kotlin.script.experimental.api.ScriptEvaluationConfiguration
import kotlin.script.experimental.api.acceptedLocations
import kotlin.script.experimental.api.defaultIdentifier
import kotlin.script.experimental.api.defaultImports
import kotlin.script.experimental.api.displayName
import kotlin.script.experimental.api.fileExtension
import kotlin.script.experimental.api.hostConfiguration
import kotlin.script.experimental.api.ide
import kotlin.script.experimental.api.scriptsInstancesSharing
import kotlin.script.experimental.host.ScriptingHostConfiguration
import kotlin.script.experimental.jvm.JvmScriptingHostConfigurationBuilder
import kotlin.script.experimental.jvm.compilationCache
import kotlin.script.experimental.jvm.dependenciesFromCurrentContext
import kotlin.script.experimental.jvm.jvm
import java.io.File

object CompilationConfig : ScriptCompilationConfiguration({
    defaultImports(
        Purpose.Commercial::class,
    )

    defaultImports(
        Purpose::class.qualifiedName + ".*",
        Month::class.qualifiedName + ".*",
        AgreementBuilder::class.java.packageName + ".*",
        "eu.dcotta.confis.dsl.*",
    )

    // baseClass(AgreementBuilder::class)
    fileExtension("confis.kts")
    displayName("Confis Agreement")

    defaultIdentifier("ConfisScript")

    jvm {
        dependenciesFromCurrentContext(wholeClasspath = true)
    }

    ide {
        acceptedLocations(Everywhere)
    }

    hostConfiguration(ScriptingHostConfiguration {
        jvm {
            cacheConfiguration()
        }
    })
})

object EvaluationConfig : ScriptEvaluationConfiguration({
    scriptsInstancesSharing(false) // if a script is imported multiple times in the import hierarchy, use a single copy
})

const val compiledScriptsCacheDirProperty = "eu.dcotta.confis.scripting.compilation_cache_dir"
const val compiledScriptsCacheDirEnvVar = "COMPILED_CONFIS_SCRIPTS_CACHE_DIR"
fun JvmScriptingHostConfigurationBuilder.cacheConfiguration() {
    val cacheDirSetting = System.getProperty(compiledScriptsCacheDirProperty)
        ?: System.getenv(compiledScriptsCacheDirEnvVar)

    val cacheBaseDir = when {
        cacheDirSetting == null -> System.getProperty("java.io.tempdir")
            ?.let(::File)
            ?.takeIf { it.exists() && it.isDirectory }
            ?.let { File(it, "confis.kts.compiled.cache") }
            ?.also { it.mkdirs() }
        cacheDirSetting.isBlank() || File(cacheDirSetting).let { it.exists() && !it.isDirectory } -> null
        else -> File(cacheDirSetting)
    }

    if (cacheBaseDir != null) compilationCache(InMemAndFileSystemCache(cacheBaseDir, 20))
}

