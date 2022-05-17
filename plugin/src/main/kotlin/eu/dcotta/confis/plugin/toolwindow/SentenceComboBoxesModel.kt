package eu.dcotta.confis.plugin.toolwindow

import eu.dcotta.confis.model.Action
import eu.dcotta.confis.model.Obj
import eu.dcotta.confis.model.Sentence
import eu.dcotta.confis.model.Subject
import javax.swing.DefaultComboBoxModel

class SentenceComboBoxesModel {
    val subjectsModel = DefaultComboBoxModel<Subject>()
    val actionsModel = DefaultComboBoxModel<Action>()
    val objectsModel = DefaultComboBoxModel<Obj>()

    fun <T> DefaultComboBoxModel<T>.setAll(all: Collection<T>) {
        removeAllElements()
        addAll(all)
    }

    fun setSubjects(subjects: List<Subject>) = subjectsModel.setAll(subjects)
    var selectedSubject: Subject? = null

    fun setActions(actions: List<Action>) = actionsModel.setAll(actions)
    var selectedAction: Action? = null

    fun setObjects(objs: List<Obj>) = objectsModel.setAll(objs)
    var selectedObject: Obj? = null

    val questionReady: Boolean
        get() = selectedObject != null && selectedAction != null && selectedSubject != null

    fun copy() = SentenceComboBoxesModel().also {
        it.subjectsModel.addAll(subjectsModel.getAll())
        it.actionsModel.addAll(actionsModel.getAll())
        it.objectsModel.addAll(objectsModel.getAll())
        it.selectedSubject = selectedSubject
        it.selectedAction = selectedAction
        it.selectedObject = selectedObject
    }

    fun sentence(): Sentence? = selectedSubject?.let { subject ->
        selectedAction?.let { action ->
            selectedObject?.let { obj ->
                Sentence(subject, action, obj)
            }
        }
    }

    fun questionButtonChangeListener(listener: (enabled: Boolean) -> Unit) {}

    private fun <T> DefaultComboBoxModel<T>.getAll(): List<T> = (0 until size).map(::getElementAt)
}
