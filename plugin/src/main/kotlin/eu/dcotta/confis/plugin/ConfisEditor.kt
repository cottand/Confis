package eu.dcotta.confis.plugin

import com.intellij.openapi.fileEditor.FileEditor
import com.intellij.openapi.fileEditor.TextEditor
import com.intellij.openapi.fileEditor.TextEditorWithPreview
import com.intellij.openapi.util.Key

class ConfisEditor(val editor: TextEditor, val preview: FileEditor) : TextEditorWithPreview(editor, preview) {
    val PARENT_SPLIT_EDITOR_KEY: Key<ConfisEditor> = Key.create("parentSplit")

    init {
        editor.putUserData(PARENT_SPLIT_EDITOR_KEY, this)
        preview.putUserData(PARENT_SPLIT_EDITOR_KEY, this)

        // preview.set
        // see https://github.com/JetBrains/intellij-community/blob/master/plugins/markdown/core/src/org/intellij/plugins/markdown/ui/preview/MarkdownEditorWithPreview.java
        // https://intellij-support.jetbrains.com/hc/en-us/community/posts/4629796215698-How-to-create-a-SplitEditorToolbar-in-Intellij-IDEA-plugin-
    }
}
