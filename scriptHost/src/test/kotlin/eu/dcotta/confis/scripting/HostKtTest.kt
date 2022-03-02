package eu.dcotta.confis.scripting

import io.kotest.assertions.fail
import io.kotest.core.spec.style.StringSpec
import kotlin.script.experimental.api.ResultWithDiagnostics
import java.io.File

class HostKtTest : StringSpec({
    "compile well-formed script" {

        val res = evalFile(File("testScripts/simple.confis.kts"))

        if (res !is ResultWithDiagnostics.Success) fail(
            "test failed:\n  ${res.reports.joinToString("\n  ") { it.message + if (it.exception == null) "" else ": ${it.exception}" }}")

    }
})
