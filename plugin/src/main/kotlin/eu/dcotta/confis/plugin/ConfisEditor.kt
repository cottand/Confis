package eu.dcotta.confis.plugin

import com.intellij.openapi.application.WriteAction
import com.intellij.openapi.diagnostic.debug
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.editor.event.DocumentEvent
import com.intellij.openapi.editor.event.DocumentListener
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.fileEditor.TextEditor
import com.intellij.openapi.fileEditor.TextEditorWithPreview
import com.intellij.openapi.fileEditor.TextEditorWithPreview.Layout.SHOW_EDITOR_AND_PREVIEW
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.testFramework.LightVirtualFile
import com.intellij.util.Alarm
import com.intellij.util.Alarm.ThreadToUse.POOLED_THREAD
import com.intellij.util.Alarm.ThreadToUse.SWING_THREAD
import org.intellij.plugins.markdown.ui.preview.MarkdownPreviewFileEditor

class ConfisEditor(
    editor: TextEditor,
    private val confisFile: VirtualFile,
    private val preview: MarkdownPreviewFileEditor,
    mdInMem: LightVirtualFile,
    val project: Project,
    private val scriptHost: ConfisHost,
) :
    TextEditorWithPreview(editor, preview, "ConfisEditor", SHOW_EDITOR_AND_PREVIEW, false) {

    val PARENT_SPLIT_EDITOR_KEY: Key<ConfisEditor> = Key.create("parentSplit")

    private val scriptDocument = FileDocumentManager.getInstance().getDocument(confisFile)
    private val logger = logger<ConfisEditor>()

    init {
        editor.putUserData(PARENT_SPLIT_EDITOR_KEY, this)
        preview.putUserData(PARENT_SPLIT_EDITOR_KEY, this)

        preview.setMainEditor(editor.editor)
    }

    private val alarm = Alarm(POOLED_THREAD, this)
    private val uiAlarm = Alarm(SWING_THREAD, this)
    private val docFactory = FileDocumentManager.getInstance()
    private val mdDocument = docFactory.getDocument(mdInMem)

    private fun documentToMarkdown(event: DocumentEvent): String {
        val source = confisFile.asConfisSourceCode(event.document.text)

        return scriptHost.eval(source).renderMarkdownResult()
    }

    private val scriptListener = DocumentListenerImpl(
        beforeDocChange = {
            alarm.cancelAllRequests()
            uiAlarm.cancelAllRequests()
        },
        afterDocChange = { event ->
            alarm.request(delayMillis = 100) {
                val md = documentToMarkdown(event)
                uiAlarm.request {
                    WriteAction.run<Exception> {
                        mdDocument?.setText(md)
                    }
                    preview.selectNotify()
                    logger.debug { "Confis set markdown from ${confisFile.name}" }
                }
            }
        }
    )

    init {
        scriptDocument?.addDocumentListener(scriptListener)
    }

    override fun dispose() {
        alarm.cancelAllRequests()
        scriptDocument?.removeDocumentListener(scriptListener)
    }
}

data class DocumentListenerImpl(
    val beforeDocChange: (DocumentEvent) -> Unit,
    val afterDocChange: (DocumentEvent) -> Unit,
) : DocumentListener {
    override fun beforeDocumentChange(event: DocumentEvent) = beforeDocChange(event)
    override fun documentChanged(event: DocumentEvent) = afterDocChange(event)
}
