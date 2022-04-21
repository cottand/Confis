package eu.dcotta.confis.plugin

import com.intellij.openapi.fileEditor.FileEditor
import com.intellij.openapi.fileEditor.FileEditorPolicy
import com.intellij.openapi.fileEditor.FileEditorPolicy.PLACE_AFTER_DEFAULT_EDITOR
import com.intellij.openapi.fileEditor.FileEditorProvider
import com.intellij.openapi.fileEditor.TextEditor
import com.intellij.openapi.fileEditor.impl.text.TextEditorProvider
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiFileFactory
import org.intellij.plugins.markdown.lang.MarkdownLanguage
import org.intellij.plugins.markdown.ui.preview.MarkdownPreviewFileEditor

class ConfisEditorProvider : FileEditorProvider {
    override fun accept(project: Project, file: VirtualFile): Boolean {
        return file.name.endsWith("confis.kts")
    }

    override fun createEditor(project: Project, file: VirtualFile): FileEditor {
        val editor: TextEditor = TextEditorProvider.getInstance().createEditor(project, file) as TextEditor

        val mdFileName = "${file.name}_temp-confis.md"
        val mdLang = MarkdownLanguage.INSTANCE
        //val mdInMem = LightVirtualFile(mdFileName)
        val factory = PsiFileFactory.getInstance(project)

        // preserve \r\n as it is done in MultiHostRegistrarImpl
        val mdPsi = factory.createFileFromText(mdFileName, mdLang, "# init md1", true, false)
        val mdInMem = (mdPsi.virtualFile as? com.intellij.testFramework.LightVirtualFile)!!
        //myNewVirtualFile.setOriginalFile(injectedFile.getVirtualFile())

        assert(mdPsi.textLength == mdInMem.content.length) { "PSI / Virtual file text mismatch" }

        val preview = MarkdownPreviewFileEditor(project, mdInMem)

        return ConfisEditor(editor, file, preview, mdInMem, project)
    }

    override fun getEditorTypeId() = id

    override fun getPolicy(): FileEditorPolicy = PLACE_AFTER_DEFAULT_EDITOR

    companion object {
        const val id = "confis-editor-preview"
    }
}
