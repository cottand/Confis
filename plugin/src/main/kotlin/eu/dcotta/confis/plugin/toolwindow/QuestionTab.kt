package eu.dcotta.confis.plugin.toolwindow

import com.intellij.codeInspection.javaDoc.JavadocUIUtil.bindItem
import com.intellij.ui.dsl.builder.Panel
import java.awt.Component
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

class ScrollPane : JPanel(GridBagLayout()) {

    init {
        add(JPanel(), Companion.gbc)
    }

    val children: MutableList<Component> = mutableListOf()

    fun addScroll(panel: JComponent) {

        add(panel, Companion.gbc, 0)
        children += panel

        validate()
        repaint()
    }

    fun clear() {
        children.forEach(::remove)
        children.clear()
    }

    companion object {
        private val gbc = GridBagConstraints().apply {
            weightx = 1.0
            weighty = 1.0
            gridwidth = GridBagConstraints.REMAINDER
            gridheight = GridBagConstraints.NORTH
        }
    }
}
