package scripting

import eu.dcotta.confis.scripting.ConfisScriptDefinition
import io.kotest.core.spec.style.StringSpec
import java.io.File
import kotlin.script.experimental.api.EvaluationResult
import kotlin.script.experimental.api.ResultWithDiagnostics
import kotlin.script.experimental.host.ScriptingHostConfiguration
import kotlin.script.experimental.host.toScriptSource
import kotlin.script.experimental.jvm.dependenciesFromCurrentContext
import kotlin.script.experimental.jvm.jvm
import kotlin.script.experimental.jvmhost.BasicJvmScriptingHost
import kotlin.script.experimental.jvmhost.createJvmCompilationConfigurationFromTemplate
import kotlin.time.ExperimentalTime
import kotlin.time.measureTime

@OptIn(ExperimentalTime::class)
class PerformanceTests : StringSpec({
    val compilationConfiguration = createJvmCompilationConfigurationFromTemplate<ConfisScriptDefinition> {
        jvm {
            dependenciesFromCurrentContext(wholeClasspath = true)
        }
    }
    val host = BasicJvmScriptingHost(ScriptingHostConfiguration {})

    fun evalFile(scriptFile: File): ResultWithDiagnostics<EvaluationResult> {
        return host.eval(scriptFile.toScriptSource(), compilationConfiguration, null)
    }
    "geophys 1" {
        val source = File("src/test/resources/scripts/simple.confis.kts").toScriptSource()

        val sourceReading = measureTime { source.text }
    }
})
