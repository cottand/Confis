package eu.dcotta.confis.plugin

import com.intellij.ide.script.IdeScriptEngineManager
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
import eu.dcotta.confis.dsl.AgreementBuilder
import eu.dcotta.confis.model.Agreement
import eu.dcotta.confis.render.renderMarkdown
import eu.dcotta.confis.scripting.ConfisScriptDefinition
import kotlin.script.experimental.api.ResultWithDiagnostics
import kotlin.script.experimental.api.SourceCode
import kotlin.script.experimental.jvm.BasicJvmScriptEvaluator
import kotlin.script.experimental.jvmhost.BasicJvmScriptingHost
import kotlin.script.experimental.jvmhost.createJvmCompilationConfigurationFromTemplate
import org.intellij.plugins.markdown.ui.preview.MarkdownPreviewFileEditor
import org.jetbrains.kotlin.diagnostics.rendering.DefaultErrorMessages
import org.jetbrains.kotlin.scripting.resolve.VirtualFileScriptSource

class ConfisEditor(
    val editor: TextEditor,
    val confisFile: VirtualFile,
    val preview: MarkdownPreviewFileEditor,
    val mdInMem: LightVirtualFile,
    val project: Project,
) :
    TextEditorWithPreview(editor, preview, "ConfisEditor", SHOW_EDITOR_AND_PREVIEW, true) {

    val PARENT_SPLIT_EDITOR_KEY: Key<ConfisEditor> = Key.create("parentSplit")

    val markdownDocument = FileDocumentManager.getInstance().getDocument(mdInMem)
    val scriptDocument = FileDocumentManager.getInstance().getDocument(confisFile)

    init {
        editor.putUserData(PARENT_SPLIT_EDITOR_KEY, this)
        preview.putUserData(PARENT_SPLIT_EDITOR_KEY, this)

        preview.setMainEditor(editor.editor)

        // preview.set
        // see https://github.com/JetBrains/intellij-community/blob/master/plugins/markdown/core/src/org/intellij/plugins/markdown/ui/preview/MarkdownEditorWithPreview.java
        // https://intellij-support.jetbrains.com/hc/en-us/community/posts/4629796215698-How-to-create-a-SplitEditorToolbar-in-Intellij-IDEA-plugin-
    }

    private val alarm = Alarm(POOLED_THREAD, this)

    val host = ConfisHost()
    val host2 = IdeScriptEngineManager.getInstance().getEngineByName("kotlin", null)?.e

    private fun documentToMarkdown(event: DocumentEvent): String? {
        val source = ConfisSourceCode(confisFile.url, confisFile.name, event.document.text)

        return host.eval(source)?.renderMarkdown()
    }

    val scriptListener = object : DocumentListener {
        override fun beforeDocumentChange(event: DocumentEvent) {
            alarm.cancelAllRequests()
        }

        override fun documentChanged(event: DocumentEvent) {
            alarm.addRequest({
                documentToMarkdown(event)?.let { md ->
                    markdownDocument?.setText(md)
                    mdInMem.setContent(this, md, true)
                    editor.selectNotify()
                }
            }, 10)
        }
    }

    init {
        scriptDocument?.addDocumentListener(scriptListener)
    }

    override fun dispose() {
        alarm.cancelAllRequests()
        scriptDocument?.removeDocumentListener(scriptListener)
    }
}

class ConfisHost {
    val compilationConfiguration = createJvmCompilationConfigurationFromTemplate<ConfisScriptDefinition>()
    val defaultHost = BasicJvmScriptingHost()

    fun eval(script: SourceCode): Agreement? {
        val res = defaultHost.eval(script, compilationConfiguration, null)
        if (res !is ResultWithDiagnostics.Success null

        val instance = res.value.returnValue.scriptInstance

        val i = (instance as AgreementBuilder)

        return AgreementBuilder.assemble(i)
    }
}
