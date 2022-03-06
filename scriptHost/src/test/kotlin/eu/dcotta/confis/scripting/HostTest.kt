package eu.dcotta.confis.scripting

import eu.dcotta.confis.dsl.AgreementBuilder
import eu.dcotta.confis.model.Clause
import io.kotest.assertions.fail
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.should
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.types.beOfType
import kotlin.script.experimental.api.ResultWithDiagnostics
import java.io.File

class HostTest : StringSpec({
    "compile well-formed script" {

        val res = evalFile(File("src/test/resources/scripts/simple.confis.kts"))

        if (res !is ResultWithDiagnostics.Success) fail(
            "test failed:\n  ${res.reports.joinToString("\n  ") { it.message + if (it.exception == null) "" else ": ${it.exception}" }}")

        val i = (res.value.returnValue.scriptInstance as Definition)

        val assembled = AgreementBuilder.assemble(i)
        println(assembled)

        assembled.clauses shouldNotBe emptyList<Clause>()
    }

    "fails when trying to specify purposes on a deny clause" {
        val res = evalFile(File("src/test/resources/scripts/purposeOnForbid.confis.kts"))

        res should beOfType<ResultWithDiagnostics.Failure>()
    }
})
