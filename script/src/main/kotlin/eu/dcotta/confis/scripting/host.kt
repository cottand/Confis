package eu.dcotta.confis.scripting

import java.io.File
import kotlin.script.experimental.api.EvaluationResult
import kotlin.script.experimental.api.ResultWithDiagnostics
import kotlin.script.experimental.host.ScriptingHostConfiguration
import kotlin.script.experimental.host.toScriptSource
import kotlin.script.experimental.jvm.dependenciesFromCurrentContext
import kotlin.script.experimental.jvm.jvm
import kotlin.script.experimental.jvmhost.BasicJvmScriptingHost
import kotlin.script.experimental.jvmhost.createJvmCompilationConfigurationFromTemplate

private val compilationConfiguration = createJvmCompilationConfigurationFromTemplate<ConfisScriptDefinition> {
    jvm {
        dependenciesFromCurrentContext(wholeClasspath = true)
    }
}
private val host = BasicJvmScriptingHost(
    ScriptingHostConfiguration {
        jvm {
             hybridCacheConfiguration()
        }
    }
)

fun evalFile(scriptFile: File): ResultWithDiagnostics<EvaluationResult> {
    return host.eval(scriptFile.toScriptSource(), compilationConfiguration, null)
}

fun main() {
    TODO()
}
