package eu.dcotta.confis.plugin.toolwindow

import com.intellij.codeInspection.javaDoc.JavadocUIUtil.bindItem
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.dsl.builder.Panel
import java.awt.BorderLayout
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import javax.swing.JComponent
import javax.swing.JPanel

interface QuestionTab {
    val title: String
    val component: JComponent
    val listener: ConfisAgreementListener? get() = null
}

fun Panel.sentenceGroup(
    model: SentenceComboBoxesModel,
) =
    group("Sentence") {
        row("Subject") {
            comboBox(model.subjectsModel).bindItem(model::selectedSubject)
        }
        row("Action") {
            comboBox(model.actionsModel).bindItem(model::selectedAction)
        }
        row("Object") {
            comboBox(model.objectsModel).bindItem(model::selectedObject)
        }
    }

class ScrollPane : JPanel() {

    private val gbc = GridBagConstraints().apply {
        weightx = 1.0
        weighty = 1.0
        gridwidth = GridBagConstraints.REMAINDER
    }

    init {
        layout = BorderLayout()
    }

    val mainList = JPanel(GridBagLayout()).also {
        it.add(JPanel(), gbc)
    }

    init {
        add(JBScrollPane(mainList))
    }

    fun addScroll(panel: JComponent) {

        val container = JPanel()

        container.add(panel)

        mainList.add(container, gbc, 0)

        validate()
        repaint()
    }
}
