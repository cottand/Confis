package eu.dcotta.confis.plugin

import com.intellij.openapi.fileEditor.FileEditor
import com.intellij.openapi.fileEditor.FileEditorPolicy
import com.intellij.openapi.fileEditor.FileEditorPolicy.PLACE_AFTER_DEFAULT_EDITOR
import com.intellij.openapi.fileEditor.FileEditorProvider
import com.intellij.openapi.fileEditor.TextEditor
import com.intellij.openapi.fileEditor.impl.text.TextEditorProvider
import com.intellij.openapi.fileTypes.FileType
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.testFramework.LightVirtualFile
import org.intellij.plugins.markdown.lang.MarkdownFileType
import org.intellij.plugins.markdown.lang.MarkdownLanguage
import org.intellij.plugins.markdown.ui.preview.MarkdownPreviewFileEditor
import org.jetbrains.kotlin.idea.debugger.coroutine.util.logger

class ConfisEditorProvider : FileEditorProvider {
    override fun accept(project: Project, file: VirtualFile): Boolean {
        return file.name.endsWith("confis.kts")
    }

    override fun createEditor(project: Project, file: VirtualFile): FileEditor {
        val editor: TextEditor = TextEditorProvider.getInstance().createEditor(project, file) as TextEditor

        val t = """
            # Confis Agreement

            ## 1 - Parties

            1. **alice**
                    
            ## 2 - Definitions

            1. _"notify"_: Notify by email
            2. _"pay"_

            ## 3 - Terms

            1. alice may notify alice under the following circumstances:
            
                  1. only after alice did pay alice
                  2. Policies[Commercial]
                  
                 
            2. alice may pay alice
            3. Some useless text clause

        """.trimIndent()


        val mdInMem = LightVirtualFile("${file.name}_temp-confis.md")
        mdInMem.language = MarkdownLanguage.INSTANCE
        val preview = MarkdownPreviewFileEditor(project, mdInMem)

        return ConfisEditor(editor, file, preview, mdInMem, project)
    }

    override fun getEditorTypeId() = id

    override fun getPolicy(): FileEditorPolicy = PLACE_AFTER_DEFAULT_EDITOR

    companion object {
        const val id = "confis-editor-preview"
    }
}
