package eu.dcotta.confis.plugin.toolwindow

import com.intellij.openapi.Disposable
import com.intellij.openapi.project.Project
import eu.dcotta.confis.eval.QueryResponse
import eu.dcotta.confis.eval.allowance.AllowanceQuestion
import eu.dcotta.confis.eval.allowance.ask
import eu.dcotta.confis.eval.compliance.ComplianceQuestion
import eu.dcotta.confis.eval.compliance.ask
import eu.dcotta.confis.model.Action
import eu.dcotta.confis.model.Agreement
import eu.dcotta.confis.model.Obj
import eu.dcotta.confis.model.Sentence
import eu.dcotta.confis.model.Subject
import eu.dcotta.confis.plugin.ConfisCompiledNotifier
import eu.dcotta.confis.plugin.toolwindow.ConfisQueryType.Allowance
import eu.dcotta.confis.plugin.toolwindow.ConfisQueryType.Compliance
import kotlin.script.experimental.api.ResultWithDiagnostics.Success

data class ConfisAgreementListener(
    val onSubjectsUpdated: (List<Subject>) -> Unit,
    val onActionsUpdated: (Set<Action>) -> Unit,
    val onObjectsUpdated: (Set<Obj>) -> Unit,
    val onResults: (List<String>) -> Unit,
    val onCircumstances: (List<String>) -> Unit,
    val onDocument: (name: String?) -> Unit,
)

class QuestionWindowModel(project: Project) : Disposable {
    private val listeners: MutableList<ConfisAgreementListener> = mutableListOf()

    var latestAgreement: Agreement? = null

    private val topicListener = ConfisCompiledNotifier { file, result ->
        if (result is Success) {
            latestAgreement = result.value
            for (l in listeners) {
                val a = result.value
                l.onDocument(file.name)
                l.onSubjectsUpdated(a.parties)
                l.onActionsUpdated(a.actions)
                l.onObjectsUpdated(a.objs)
            }
        } else for (l in listeners) l.onDocument(null)
    }

    init {
        project.messageBus.connect(this).subscribe(ConfisCompiledNotifier.CHANGE_ACTION_TOPIC, topicListener)
        // ConfisCompiledNotifier.CHANGE_ACTION_TOPIC.subscribe(this, topicListener)
    }

    fun addListener(listener: ConfisAgreementListener) {
        listeners += listener
    }

    // fun removeListener(listener: ConfisAgreementListener) {
    //    listeners -= listener
    // }
    //
    // fun addCircumstance(c: Circumstance) {
    // }
    //
    // fun removeCircumstances(cs: List<Circumstance>) {
    // }

    fun ask(type: ConfisQueryType, sentence: Sentence): QueryResponse? =
        latestAgreement?.let { agreement ->
            when (type) {
                Allowance -> agreement.ask(AllowanceQuestion(sentence))
                Compliance -> agreement.ask(ComplianceQuestion())
            }
        }

    override fun dispose() {
    }
}

enum class ConfisQueryType {
    Allowance, Compliance;
}
