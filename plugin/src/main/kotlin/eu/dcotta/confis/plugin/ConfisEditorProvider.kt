package eu.dcotta.confis.plugin

import com.intellij.openapi.application.WriteAction
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
import com.intellij.util.Alarm
import com.intellij.util.Alarm.ThreadToUse.SWING_THREAD
import eu.dcotta.confis.scripting.CONFIS_FILE_EXTENSION
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.intellij.plugins.markdown.lang.MarkdownLanguage
import org.intellij.plugins.markdown.ui.preview.MarkdownPreviewFileEditor

class ConfisEditorProvider : FileEditorProvider {
    override fun accept(project: Project, file: VirtualFile): Boolean =
        file.name.endsWith(CONFIS_FILE_EXTENSION)

    private val tempScope = CoroutineScope(Dispatchers.Default)
    private val uiAlarm = Alarm(SWING_THREAD)

    override fun createEditor(project: Project, file: VirtualFile): FileEditor {

        val editor: TextEditor = TextEditorProvider.getInstance().createEditor(project, file) as TextEditor

        val confisText = FileDocumentManager.getInstance().getDocument(file)?.text ?: ""

        val mdFileName = "${file.name}_temp-confis.md"
        val mdLang = MarkdownLanguage.INSTANCE
        val mdInMem = LightVirtualFile(mdFileName, "")
        mdInMem.language = mdLang

        val preview = MarkdownPreviewFileEditor(project, mdInMem)

        val confisEditor = ConfisEditor(editor, file, preview, mdInMem, project)

        // so we do not interrupt UI
        tempScope.launch {
            val agreement = Resources.scriptHost.eval(file.asConfisSourceCode(confisText))
            // to notify to tool window
            project.messageBus.syncPublisher(ConfisCompiledNotifier.CHANGE_ACTION_TOPIC)
                .afterCompile(file, agreement)

            val initialMdText = agreement.renderMarkdownResult()
            uiAlarm.request {
                val doc = FileDocumentManager.getInstance().getDocument(mdInMem)
                WriteAction.run<Exception> {
                    doc?.setText(initialMdText)
                }
                if (confisEditor.latestAgreement == null) {
                    confisEditor.latestAgreement = agreement
                }
            }
        }
        return confisEditor
    }

    override fun getEditorTypeId() = id

    override fun getPolicy(): FileEditorPolicy = PLACE_BEFORE_DEFAULT_EDITOR

    companion object {
        const val id = "confis-editor-preview"
    }
}
