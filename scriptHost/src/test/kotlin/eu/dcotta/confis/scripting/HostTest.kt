package eu.dcotta.confis.scripting

import eu.dcotta.confis.dsl.LicenseBuilder
import eu.dcotta.confis.model.Clause
import eu.dcotta.confis.model.Clause.PurposePolicies
import eu.dcotta.confis.model.LegalException.ForceMajeure
import eu.dcotta.confis.model.Purpose.Commercial
import eu.dcotta.confis.model.PurposePolicy
import io.kotest.assertions.fail
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldContain
import kotlin.script.experimental.api.ResultWithDiagnostics
import java.io.File

class HostTest : StringSpec({
    "compile well-formed script" {

        val res = evalFile(File("src/test/scripts/simple.confis.kts"))

        if (res !is ResultWithDiagnostics.Success) fail(
            "test failed:\n  ${res.reports.joinToString("\n  ") { it.message + if (it.exception == null) "" else ": ${it.exception}" }}")

        val i = (res.value.returnValue.scriptInstance as Definition)

        val assembled = LicenseBuilder.assemble(i)
        println(assembled)

        assembled.clauses shouldContain
            Clause.WithExceptions(
                clause = PurposePolicies(PurposePolicy.Forbid(Commercial)),
                exception = listOf(ForceMajeure)
            )
    }
})
