package eu.dcotta.confis.plugin

import eu.dcotta.confis.scripting.CompilationConfig
import eu.dcotta.confis.scripting.EvaluationConfig
import kotlin.script.experimental.host.ScriptDefinition
import kotlin.script.experimental.host.ScriptingHostConfiguration
import kotlin.script.experimental.intellij.ScriptDefinitionsProvider
import java.io.File

class ConfisDefinitionProvider : ScriptDefinitionsProvider {

    override val id: String get() = "eu.dcotta.confis"

    override fun getDefinitionClasses(): Iterable<String> = listOf(
        "eu.dcotta.confis.scripting.Definition"
    )

    override fun getDefinitionsClassPath(): Iterable<File> = listOf(File("lib/script"))

    override fun useDiscovery(): Boolean = true

    val def = ScriptDefinition(
        CompilationConfig,
        EvaluationConfig,
    )

    override fun provideDefinitions(
        baseHostConfiguration: ScriptingHostConfiguration,
        loadedScriptDefinitions: List<ScriptDefinition>,
    ): Iterable<ScriptDefinition> =
        loadedScriptDefinitions.plus(def)
}
