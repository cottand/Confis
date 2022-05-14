package eu.dcotta.confis.plugin.toolwindow

import com.intellij.ui.components.JBTabbedPane
import com.intellij.ui.dsl.builder.bindText
import com.intellij.ui.dsl.builder.panel
import com.intellij.ui.dsl.gridLayout.VerticalAlign.FILL
import javax.swing.SwingConstants

class ToolWindowView(
    val model: QuestionWindowModel,
    val tabs: List<QuestionTab>,
) {

    val content = panel {
        row {
            label("Please open a valid confis agreement").bindText(model._currentDocTitle)
        }
        row {
            val tabbedPane = JBTabbedPane(SwingConstants.TOP)
            for (t in tabs) tabbedPane.add(t.title, t.component)
            cell(tabbedPane)
                .resizableColumn()
                .verticalAlign(FILL)
        }
    }
}
