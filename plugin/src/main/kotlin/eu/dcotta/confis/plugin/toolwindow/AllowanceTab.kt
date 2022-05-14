package eu.dcotta.confis.plugin.toolwindow

import com.intellij.ui.CollectionListModel
import com.intellij.ui.dsl.builder.RightGap.SMALL
import com.intellij.ui.dsl.builder.panel
import com.intellij.ui.dsl.gridLayout.HorizontalAlign.CENTER
import com.intellij.ui.dsl.gridLayout.HorizontalAlign.FILL
import eu.dcotta.confis.model.Sentence
import javax.swing.JList

class AllowanceTab(
    val circumstanceEditor: CircumstanceEditor,
    val questionWindowModel: QuestionWindowModel,
) : QuestionTab {

    val model = SentenceComboBoxesModel()

    override val title get() = "Allowance"

    override val listener = ConfisAgreementListener(
        onSubjectsUpdated = model::setSubjects,
        onActionsUpdated = model::setActions,
        onObjectsUpdated = model::setObjects,
    )

    val results = CollectionListModel<String>()

    private fun askQuestion() {
        if (model.questionReady) {
            val s = Sentence(model.selectedSubject!!, model.selectedAction!!, model.selectedObject!!)
            questionWindowModel.askAllowance(s, circumstanceEditor.expression) {
                results.add(0, it.render())
            }
        }
    }

    override val component = panel {
        gap(SMALL)
        row {
            comment("Is S allowed under the given circumstances?").horizontalAlign(CENTER)
        }
        sentenceGroup(model)

        group("Circumstances") {
            resizableColumn()
            row {
                scrollCell(circumstanceEditor.editorComponent)
                    .gap(SMALL)
                    .resizableColumn()
                    .horizontalAlign(FILL)
            }
        }
        indent {
            row {
                button("Ask Allowance Question") {
                    askQuestion()
                }.bold().horizontalAlign(FILL)
            }
        }
        group("Results") {
            row {
                scrollCell(JList(results))
                    .resizableColumn()
                    .horizontalAlign(FILL)
            }
        }
    }
}
