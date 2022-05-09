package eu.dcotta.confis.plugin

import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import javax.swing.JPanel
import javax.swing.JScrollPane

class QuestionToolWindow(val toolWindow: ToolWindow, val project: Project) {
    private val fileEditorManager: FileEditorManager = FileEditorManager.getInstance(project)

    val selectedEditor get() = fileEditorManager.selectedEditor

    lateinit var questionTypeScrollPane: JScrollPane
    lateinit var content: JPanel
}
