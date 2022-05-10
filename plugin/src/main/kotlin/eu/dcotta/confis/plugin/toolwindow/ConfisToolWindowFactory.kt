package eu.dcotta.confis.plugin.toolwindow

import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.ContentFactory.SERVICE

class ConfisToolWindowFactory : ToolWindowFactory {
    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val model = ToolWindowModel(project)

        val myToolWindow = QuestionToolWindow(toolWindow, project)

        model.addListener(
            ConfisAgreementListener(
                onSubjectsUpdated = myToolWindow::setSubjects,
                onActionsUpdated = myToolWindow::setActions,
                onCircumstances = {},
                onDocument = myToolWindow::setDocName,
                onResults = {},
                onObjectsUpdated = myToolWindow::setObjects,
            )
        )

        val contentFactory = SERVICE.getInstance()
        val content = contentFactory.createContent(myToolWindow.content, "", false)
        toolWindow.contentManager.addContent(content)
    }
}
