package eu.dcotta.confis.plugin

import com.intellij.testFramework.LightVirtualFile
import io.kotest.assertions.fail
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import kotlin.script.experimental.api.ResultWithDiagnostics

// FIXME bloody classpath troubles
class ConfisHostTest : StringSpec({
    "can compile empty script from memory and cast to agreement".config(enabled = false) {
        val ktFile = LightVirtualFile("hello.confis.kts", "")
        val compiled = ConfisHost().eval(ktFile.asConfisSourceCode(""))
        if (compiled !is ResultWithDiagnostics.Success) fail("Should successfully compile")
        compiled.value.parties shouldBe emptyList()
    }
})
