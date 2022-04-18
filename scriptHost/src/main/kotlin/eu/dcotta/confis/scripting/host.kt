package eu.dcotta.confis.scripting

import eu.dcotta.confis.dsl.AgreementBuilder
import eu.dcotta.confis.model.Agreement
import java.io.File
import kotlin.script.experimental.api.EvaluationResult
import kotlin.script.experimental.api.ResultWithDiagnostics
import kotlin.script.experimental.api.SourceCode
import kotlin.script.experimental.host.toScriptSource
import kotlin.script.experimental.jvm.dependenciesFromCurrentContext
import kotlin.script.experimental.jvm.jvm
import kotlin.script.experimental.jvmhost.BasicJvmScriptingHost
import kotlin.script.experimental.jvmhost.createJvmCompilationConfigurationFromTemplate

fun evalFile(scriptFile: File): ResultWithDiagnostics<EvaluationResult> {
    val compilationConfiguration = createJvmCompilationConfigurationFromTemplate<ConfisScriptDefinition> {
        jvm {
            dependenciesFromCurrentContext(
                "lib", "script"
            )
        }
    }

    return BasicJvmScriptingHost().eval(scriptFile.toScriptSource(), compilationConfiguration, null)
}

class ConfisHost {
    val compilationConfiguration = createJvmCompilationConfigurationFromTemplate<ConfisScriptDefinition>()
    val defaultHost = BasicJvmScriptingHost()

    fun eval(script: SourceCode): Agreement? {
        val res = defaultHost.eval(script, compilationConfiguration, null)
        if (res !is ResultWithDiagnostics.Success) return null

        val instance = res.value.returnValue.scriptInstance

        val i = (instance as AgreementBuilder)

        return AgreementBuilder.assemble(i)
    }
}


fun main() {
    TODO()
}
