package eu.dcotta.confis.plugin

import com.intellij.testFramework.LightVirtualFile
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.mockk.mockk

class ConfisEditorProviderTest : StringSpec({

    System.setProperty("idea.force.use.core.classloader", "true")

    "accepts confis kts files only" {
        val p = ConfisEditorProvider()

        p.accept(mockk(), LightVirtualFile("yolo.confis.kts")) shouldBe true

        p.accept(mockk(), LightVirtualFile("main.kts")) shouldBe false
        p.accept(mockk(), LightVirtualFile("a.md")) shouldBe false
    }
})
