package eu.dcotta.confis.plugin

import com.intellij.testFramework.LightVirtualFile
import eu.dcotta.confis.scripting.ConfisScriptDefinition
import io.kotest.assertions.fail
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kotlin.script.experimental.api.EvaluationResult
import kotlin.script.experimental.api.ResultValue
import kotlin.script.experimental.api.ResultWithDiagnostics
import kotlin.script.experimental.jvmhost.BasicJvmScriptingHost

class ConfisHostTest : StringSpec({
    val ktFile = LightVirtualFile("hello.confis.kts", "")

    "can compile empty script from memory and cast to agreement".config(enabled = true) {
        val a = ConfisScriptDefinition()
        val host: BasicJvmScriptingHost = mockk {
            every { eval(any(), any(), any()) } returns ResultWithDiagnostics.Success(
                EvaluationResult(ResultValue.Value("hello.confis.kts", Unit, "", a::class, a), null)
            )
        }
        val compiled = ConfisHost(host).eval(ktFile.asConfisSourceCode(""))
        if (compiled !is ResultWithDiagnostics.Success) fail("Should successfully compile")
        compiled.value.parties shouldBe emptyList()
    }
})
