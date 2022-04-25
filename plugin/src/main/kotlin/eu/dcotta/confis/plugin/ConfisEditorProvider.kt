package eu.dcotta.confis.plugin

import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.fileEditor.FileEditor
import com.intellij.openapi.fileEditor.FileEditorPolicy
import com.intellij.openapi.fileEditor.FileEditorPolicy.PLACE_BEFORE_DEFAULT_EDITOR
import com.intellij.openapi.fileEditor.FileEditorProvider
import com.intellij.openapi.fileEditor.TextEditor
import com.intellij.openapi.fileEditor.impl.text.TextEditorProvider
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.testFramework.LightVirtualFile
import eu.dcotta.confis.scripting.CONFIS_FILE_EXTENSION
import eu.dcotta.confis.scripting.hybridCacheConfiguration
import org.intellij.plugins.markdown.lang.MarkdownLanguage
import org.intellij.plugins.markdown.ui.preview.MarkdownPreviewFileEditor
import kotlin.script.experimental.host.ScriptingHostConfiguration
import kotlin.script.experimental.jvm.baseClassLoader
import kotlin.script.experimental.jvm.jvm
import kotlin.script.experimental.jvmhost.BasicJvmScriptingHost

class ConfisEditorProvider : FileEditorProvider {
    override fun accept(project: Project, file: VirtualFile): Boolean =
        file.name.endsWith(CONFIS_FILE_EXTENSION)

    override fun createEditor(project: Project, file: VirtualFile): FileEditor {
        val editor: TextEditor = TextEditorProvider.getInstance().createEditor(project, file) as TextEditor

        val ktsHost = BasicJvmScriptingHost(
            ScriptingHostConfiguration {
                jvm {
                    baseClassLoader.put(ConfisHost::class.java.classLoader)
                    // compilationCache(InMemoryCache(maxSize = 40))
                    hybridCacheConfiguration()
                }
            }
        )
        val scriptHost = ConfisHost(ktsHost)
        val confisText = FileDocumentManager.getInstance().getDocument(file)?.text ?: ""
        val initialMdText = scriptHost.eval(file.asConfisSourceCode(confisText)).renderMarkdownResult()

        val mdFileName = "${file.name}_temp-confis.md"
        val mdLang = MarkdownLanguage.INSTANCE
        val mdInMem = LightVirtualFile(mdFileName, initialMdText)
        mdInMem.language = mdLang

        val preview = MarkdownPreviewFileEditor(project, mdInMem)

        return ConfisEditor(editor, file, preview, mdInMem, project, scriptHost)
    }

    override fun getEditorTypeId() = id

    override fun getPolicy(): FileEditorPolicy = PLACE_BEFORE_DEFAULT_EDITOR

    companion object {
        const val id = "confis-editor-preview"
    }
}
