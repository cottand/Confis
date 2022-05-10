package eu.dcotta.confis.plugin.toolwindow

import com.intellij.application.subscribe
import com.intellij.openapi.Disposable
import com.intellij.openapi.project.Project
import eu.dcotta.confis.model.Action
import eu.dcotta.confis.model.Obj
import eu.dcotta.confis.model.Subject
import eu.dcotta.confis.plugin.ConfisCompiledNotifier
import kotlin.script.experimental.api.ResultWithDiagnostics.Success

data class ConfisAgreementListener(
    val onSubjectsUpdated: (List<Subject>) -> Unit,
    val onActionsUpdated: (Set<Action>) -> Unit,
    val onObjectsUpdated: (Set<Obj>) -> Unit,
    val onResults: (List<String>) -> Unit,
    val onCircumstances: (List<String>) -> Unit,
    val onDocument: (name: String?) -> Unit,
)

class ToolWindowModel(project: Project) : Disposable {
    private val listeners: MutableList<ConfisAgreementListener> = mutableListOf()

    private val topicListener = ConfisCompiledNotifier { file, result ->
        if (result is Success) for (l in listeners) {
            val a = result.value
            l.onDocument(file.name)
            l.onSubjectsUpdated(a.parties)
            l.onActionsUpdated(a.actions)
            l.onObjectsUpdated(a.objs)
        }
        else for (l in listeners) l.onDocument(null)
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

    override fun dispose() {
    }
}
