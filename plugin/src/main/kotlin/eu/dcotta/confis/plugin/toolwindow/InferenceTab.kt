package eu.dcotta.confis.plugin.toolwindow

import com.intellij.ui.dsl.builder.RightGap.SMALL
import com.intellij.ui.dsl.builder.enableIf
import com.intellij.ui.dsl.builder.panel
import com.intellij.ui.dsl.gridLayout.HorizontalAlign.CENTER
import com.intellij.ui.dsl.gridLayout.HorizontalAlign.FILL
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
        model.sentence.get()?.let { sentence ->
            questionWindowModel.askCircumstance(sentence) {
                if (it != null) sp.addScroll(
                    panel {
                        row { mdLabel(it.render()) }
                        separator()
                    }
                )
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
                }.bold().horizontalAlign(FILL).enableIf(model.questionReady)
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
