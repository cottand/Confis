package eu.dcotta.confis.plugin.toolwindow

import com.intellij.ui.CollectionListModel
import com.intellij.ui.dsl.builder.RightGap.SMALL
import com.intellij.ui.dsl.builder.enableIf
import com.intellij.ui.dsl.builder.panel
import com.intellij.ui.dsl.gridLayout.HorizontalAlign.CENTER
import com.intellij.ui.dsl.gridLayout.HorizontalAlign.FILL
import javax.swing.JList

class AllowanceTab(
    private val circumstanceEditor: CircumstanceEditor,
    private val questionWindowModel: QuestionWindowModel,
) : QuestionTab {

    val model = SentenceComboBoxesModel()

    override val title get() = "Allowance"

    override val listener = ConfisAgreementListener(
        onSubjectsUpdated = model::setSubjects,
        onActionsUpdated = model::setActions,
        onObjectsUpdated = model::setObjects,
        onNewCircumstanceContext = circumstanceEditor::setContext
    )

    val results = CollectionListModel<String>()

    private fun askQuestion() {
        model.sentence.get()?.let { sentence ->
            questionWindowModel.askAllowance(sentence, circumstanceEditor.expression) {
                if (it != null) results.add(0, it.render())
            }
        }
    }

    override val component = panel {
        gap(SMALL)
        row {
            comment("Is (Sentence) allowed under the given circumstances?").horizontalAlign(CENTER)
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
                button("Ask Allowance Question") { askQuestion() }
                    .bold()
                    .enableIf(model.questionReady)
                    .horizontalAlign(FILL)
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
