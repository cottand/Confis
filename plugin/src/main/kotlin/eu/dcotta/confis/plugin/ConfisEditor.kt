package eu.dcotta.confis.plugin

import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.notification.Notifications
import com.intellij.openapi.actionSystem.ActionGroup
import com.intellij.openapi.actionSystem.ActionPlaces
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.actionSystem.impl.ActionToolbarImpl
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
import java.awt.Dimension
import javax.swing.JComponent
import javax.swing.JLayeredPane

class ConfisEditor(
    private val editor: TextEditor,
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

    private val myComponent: JComponent by lazy {
        val editorLayerWrapper = super.getComponent()
        val actionGroup = DefaultActionGroup(AskQuestionAction())
        val toolbar = QuestionToolbar(editorLayerWrapper, actionGroup)
        editorLayerWrapper.add(toolbar, JLayeredPane.DEFAULT_LAYER)

        editorLayerWrapper
    }

    override fun getComponent(): JComponent = myComponent

    class QuestionToolbar(parentComponent: JComponent, actionGroup: ActionGroup) :
        ActionToolbarImpl(ActionPlaces.CONTEXT_TOOLBAR, actionGroup, true) {

        init {
            // Disposer.register(this, visibilityController)
            targetComponent = parentComponent
            setReservePlaceAutoPopupIcon(false)
            setMinimumButtonSize(Dimension(28, 28))
            setSkipWindowAdjustments(true)
            isOpaque = false
            layoutPolicy = NOWRAP_LAYOUT_POLICY
        }
    }

    class AskQuestionAction : AnAction("Ask Question", "Initiates a Confis Query", ConfisIcons.ConfisOrange) {
        override fun actionPerformed(e: AnActionEvent) {
            Notifications.Bus.notify(Notification("Confis Plugin", "Noti3!", NotificationType.INFORMATION))
        }
    }

    override fun dispose() {
        alarm.cancelAllRequests()
        uiAlarm.cancelAllRequests()
        scriptDocument?.removeDocumentListener(scriptListener)
        // disposes editor, preview
        super.dispose()
    }
}

data class DocumentListenerImpl(
    val beforeDocChange: (DocumentEvent) -> Unit,
    val afterDocChange: (DocumentEvent) -> Unit,
) : DocumentListener {
    override fun beforeDocumentChange(event: DocumentEvent) = beforeDocChange(event)
    override fun documentChanged(event: DocumentEvent) = afterDocChange(event)
}
