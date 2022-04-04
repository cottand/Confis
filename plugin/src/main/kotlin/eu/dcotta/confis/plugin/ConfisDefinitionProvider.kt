package eu.dcotta.confis.plugin

import eu.dcotta.confis.dsl.AgreementBuilder
import eu.dcotta.confis.scripting.ConfisScriptDefinition
import org.jetbrains.kotlin.utils.PathUtil
import java.io.File
import kotlin.script.experimental.intellij.ScriptDefinitionsProvider

class ConfisDefinitionProvider : ScriptDefinitionsProvider {

    override val id: String get() = "eu.dcotta.confis"

    override fun getDefinitionClasses(): Iterable<String> = listOf(
        ConfisScriptDefinition::class.qualifiedName!!
    )

    private inline fun <reified T> jarOf() = PathUtil.getResourcePathForClass(T::class.java)

    override fun getDefinitionsClassPath(): Iterable<File> =
        listOf(
            jarOf<ConfisScriptDefinition>(),
            jarOf<AgreementBuilder>(),
        )

    override fun useDiscovery(): Boolean = false

    // val def = ScriptDefinition(
    //    CompilationConfig,
    //    EvaluationConfig,
    // )

    // override fun provideDefinitions(
    //    baseHostConfiguration: ScriptingHostConfiguration,
    //    loadedScriptDefinitions: List<ScriptDefinition>,
    // ): Iterable<ScriptDefinition> =
    //    loadedScriptDefinitions.plus(def)
}
