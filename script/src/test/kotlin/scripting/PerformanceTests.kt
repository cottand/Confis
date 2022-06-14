package scripting

import eu.dcotta.confis.dsl.AgreementBuilder
import eu.dcotta.confis.eval.AllowanceQuestion
import eu.dcotta.confis.eval.CircumstanceQuestion
import eu.dcotta.confis.eval.ComplianceQuestion
import eu.dcotta.confis.eval.allowance.ask
import eu.dcotta.confis.eval.compliance.ask
import eu.dcotta.confis.eval.inference.ask
import eu.dcotta.confis.model.Sentence
import eu.dcotta.confis.scripting.ConfisScriptDefinition
import io.kotest.assertions.fail
import io.kotest.core.spec.style.StringSpec
import java.io.File
import kotlin.script.experimental.api.ResultWithDiagnostics.Success
import kotlin.script.experimental.host.ScriptingHostConfiguration
import kotlin.script.experimental.host.toScriptSource
import kotlin.script.experimental.jvm.dependenciesFromCurrentContext
import kotlin.script.experimental.jvm.jvm
import kotlin.script.experimental.jvmhost.BasicJvmScriptingHost
import kotlin.script.experimental.jvmhost.createJvmCompilationConfigurationFromTemplate
import kotlin.time.Duration
import kotlin.time.ExperimentalTime
import kotlin.time.measureTime
import kotlin.time.measureTimedValue

@OptIn(ExperimentalTime::class)
class PerformanceTests : StringSpec({
    val compilationConfiguration = createJvmCompilationConfigurationFromTemplate<ConfisScriptDefinition> {
        jvm {
            dependenciesFromCurrentContext(wholeClasspath = true)
        }
    }

    data class Res(
        val file: String,
        val compile: Duration,
        val assemble: Duration,
        val allowanceQ: Duration,
        val circumstanceQ: Duration,
        val complianceQ: Duration,
    )

    val host = BasicJvmScriptingHost(ScriptingHostConfiguration {})
    fun benchmark(file: File, warmup: Boolean = false): Res {
        val source = file.toScriptSource()

        val sourceReading = measureTime { source.text }

        val (evaled, evaluation) = measureTimedValue { host.eval(source, compilationConfiguration, null) }

        if (evaled !is Success) fail("Compilation should succeed")

        val (assembled, assembling) = measureTimedValue {
            val casted = evaled.value.returnValue.scriptInstance as ConfisScriptDefinition
            AgreementBuilder.assemble(casted)
        }

        val (_, cQuerying) = measureTimedValue {
            val q = ComplianceQuestion()
            assembled.ask(q)
        }
        val firstSentence = with(assembled) {
            Sentence(parties.first(), actions.first(), parties.first())
        }
        val allowanceQuerying = measureTime {
            assembled.ask(AllowanceQuestion(firstSentence))
        }

        val circumstanceQuerying = measureTime {
            assembled.ask(CircumstanceQuestion(firstSentence))
        }

        if (warmup.not()) {
            println("For file ${file.name}")
            println(" Total processing:     ${evaluation + assembling + cQuerying}")
            println("  From disk to memory: $sourceReading")
            println("  Kotlin Compilation:  $evaluation")
            println("  Confis Assembling:   $assembling")
            println("  Querying:")
            println("    Compliance:      $cQuerying")
            println("    Allowance:       $allowanceQuerying")
            println("    Circumstance:    $circumstanceQuerying\n\n")
        }

        return Res(
            file.name,
            compile = evaluation,
            assemble = assembling,
            allowanceQ = allowanceQuerying,
            circumstanceQ = circumstanceQuerying,
            complianceQ = cQuerying,
        )
    }

    fun List<Res>.avg(): Res {
        val summed = reduceRight { l, acc ->
            Res(
                l.file,
                l.compile + acc.compile,
                l.assemble + acc.assemble,
                l.allowanceQ + acc.allowanceQ,
                l.circumstanceQ + acc.circumstanceQ,
                l.complianceQ + acc.complianceQ,
            )
        }
        return Res(
            file = summed.file,
            compile = summed.compile / size,
            assemble = summed.assemble / size,
            allowanceQ = summed.allowanceQ / size,
            circumstanceQ = summed.circumstanceQ / size,
            complianceQ = summed.complianceQ / size,
        )
    }

    "benchmark".config(enabled = true) {
        val simple = File("src/test/resources/scripts/minimal.confis.kts")
        val geo = File("src/test/resources/scripts/geophys.confis.kts")
        val meat = File("src/test/resources/scripts/meat.confis.kts")
        benchmark(simple, warmup = true)
        benchmark(geo, warmup = true)

        val meatM = List(100) { benchmark(meat, warmup = true) }.avg()
        println(meatM)
        val simpleM = List(100) { benchmark(simple, warmup = true) }.avg()
        println(simpleM)
        val geoM = List(100) { benchmark(geo, warmup = true) }.avg()
        println(geoM)
    }
})
