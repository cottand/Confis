package eu.dcotta.confis.plugin.toolwindow

import com.intellij.codeInspection.javaDoc.JavadocUIUtil.bindItem
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.WindowManager
import com.intellij.ui.dsl.builder.RightGap.SMALL
import com.intellij.ui.dsl.builder.panel
import com.intellij.ui.dsl.gridLayout.HorizontalAlign.CENTER
import com.intellij.ui.dsl.gridLayout.HorizontalAlign.FILL
import eu.dcotta.confis.plugin.mdLabel
import javax.swing.JComponent

@Suppress("UnstableApiUsage")
class ComplianceTab(
    val questionWindowModel: QuestionWindowModel,
    val project: Project,
) : QuestionTab {

    override val title: String = "Compliance"

    override val listener: ConfisAgreementListener = ConfisAgreementListener(
        onNewCircumstanceContext = { psi -> worldState.forEach { it.circumstanceEditor.setContext(psi) } },
        onSubjectsUpdated = { sub -> worldState.forEach { it.sentence.setSubjects(sub) } },
        onActionsUpdated = { action -> worldState.forEach { it.sentence.setActions(action) } },
        onObjectsUpdated = { obj -> worldState.forEach { it.sentence.setObjects(obj) } },
    )

    val resultsPanel = ScrollPane()

    val statusBar = WindowManager.getInstance().getStatusBar(project)

    private fun askQuestion() {
        val state = worldState.filter { it.sentence.sentence() != null }
            .associate { it.sentence.sentence()!! to it.circumstanceEditor.expression }
        questionWindowModel.askCompliance(state) {
            if (it != null) resultsPanel.addScroll(
                panel {
                    row { mdLabel(it.render()) }
                    separator()
                }
            )
            statusBar.stopRefreshIndication()
        }
    }

    data class WorldStateEntryModel(val sentence: SentenceComboBoxesModel, val circumstanceEditor: CircumstanceEditor)

    private val worldState = mutableListOf<WorldStateEntryModel>()

    private val stateScrollPane = ScrollPane()

    override val component: JComponent = panel {
        gap(SMALL)
        row {
            comment("Is the agreement being complied with?").horizontalAlign(CENTER)
        }

        group("Past Events") {
            row {
                scrollCell(stateScrollPane)
                    .horizontalAlign(FILL)
            }
            row {
                button("Add Past Event") {
                    val editor = CircumstanceEditor(project)
                    val sentenceModel = worldState.lastOrNull()?.sentence?.copy() ?: SentenceComboBoxesModel()
                    worldState += WorldStateEntryModel(sentenceModel, editor)
                    stateScrollPane.addScroll(newStateEntry(sentenceModel, editor))
                }.horizontalAlign(CENTER)

                button("Clear Events") {
                    stateScrollPane.clear()
                    worldState.clear()
                }.horizontalAlign(CENTER)
                resizableRow()
            }
        }
        indent {
            row {
                button("Ask Compliance Question") {
                    statusBar.startRefreshIndication("Asking Confis question...")
                    askQuestion()
                }.bold().horizontalAlign(FILL)
            }
        }
        group("Results") {
            row {
                scrollCell(resultsPanel)
                    .resizableColumn()
                    .horizontalAlign(FILL)
            }
        }
    }

    companion object {
        private fun newStateEntry(sentence: SentenceComboBoxesModel, circumstanceEditor: CircumstanceEditor) = panel {
            horizontalAlign(FILL)
            resizableColumn()

            separator("Sentence")
            row {
                comboBox(sentence.subjectsModel).bindItem(sentence::selectedSubject).comment("Subject")
                comboBox(sentence.actionsModel).bindItem(sentence::selectedAction).comment("Action")
                comboBox(sentence.objectsModel).bindItem(sentence::selectedObject).comment("Object")
            }
            separator("Circumstances")
            row {
                scrollCell(circumstanceEditor.editorComponent)
                    .gap(SMALL)
                    .resizableColumn()
                    .horizontalAlign(FILL)
            }
            separator()
        }
    }
}
