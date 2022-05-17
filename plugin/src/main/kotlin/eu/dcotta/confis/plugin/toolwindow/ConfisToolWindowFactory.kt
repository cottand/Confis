package eu.dcotta.confis.plugin.toolwindow

import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.ContentFactory.SERVICE

class ConfisToolWindowFactory : ToolWindowFactory {
    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val model = QuestionWindowModel(project)

        val circumstanceEditor = CircumstanceEditor(project)
        val allowanceTab = AllowanceTab(circumstanceEditor, model)
        val tabs = listOf(allowanceTab, InferenceTab(model), ComplianceTab(model, project))
        val myToolWindow = ToolWindowView(
            model,
            tabs,
        )
        Disposer.register(toolWindow.disposable, model)

        tabs.mapNotNull { it.listener }.forEach(model::addListener)
        model.addListener(
            ConfisAgreementListener(
                onCircumstances = {},
                // onDocument = myToolWindow::setDocName,
                onResults = {},
                onNewCircumstanceContext = circumstanceEditor::setContext
            )
        )

        val contentFactory = SERVICE.getInstance()
        val content = contentFactory.createContent(myToolWindow.content, "", false)
        toolWindow.contentManager.addContent(content)
    }
}
