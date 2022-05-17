package eu.dcotta.confis.plugin.toolwindow

import com.intellij.ui.dsl.builder.RightGap.SMALL
import com.intellij.ui.dsl.builder.panel
import com.intellij.ui.dsl.gridLayout.HorizontalAlign.CENTER
import com.intellij.ui.dsl.gridLayout.HorizontalAlign.FILL
import eu.dcotta.confis.model.Sentence
import eu.dcotta.confis.plugin.mdLabel

class InferenceTab(private val questionWindowModel: QuestionWindowModel) : QuestionTab {
    override val title get() = "Circumstance"
    val model = SentenceComboBoxesModel()
    override val listener = ConfisAgreementListener(
        onSubjectsUpdated = model::setSubjects,
        onActionsUpdated = model::setActions,
        onObjectsUpdated = model::setObjects,
    )

    // val results = CollectionListModel<String>()
    val sp = ScrollPane()

    private fun askQuestion() {
        if (model.questionReady) {
            val s = Sentence(model.selectedSubject!!, model.selectedAction!!, model.selectedObject!!)
            questionWindowModel.askCircumstance(s) {
                sp.addScroll(
                    panel {
                        row {
                            mdLabel(it.render())
                        }
                        separator()
                    }
                )
                // results.add(0, it.render())
            }
        }
    }

    override val component = panel {
        gap(SMALL)
        row {
            comment("Under what circumstances may (Sentence) be performed?").horizontalAlign(CENTER)
        }

        sentenceGroup(model)

        indent {
            row {
                button("Ask Circumstance Question") {
                    askQuestion()
                }.bold().horizontalAlign(FILL)
            }
        }
        group("Results") {
            row {
                scrollCell(sp)
                    // scrollCell(JList(results))
                    .resizableColumn()
                    .horizontalAlign(FILL)
                // resizableRow()
            }
        }
    }
}
