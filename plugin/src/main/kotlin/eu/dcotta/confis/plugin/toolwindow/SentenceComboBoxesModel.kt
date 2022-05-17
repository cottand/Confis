package eu.dcotta.confis.plugin.toolwindow

import com.intellij.openapi.observable.properties.AtomicBooleanProperty
import com.intellij.openapi.observable.properties.AtomicProperty
import eu.dcotta.confis.model.Action
import eu.dcotta.confis.model.Obj
import eu.dcotta.confis.model.Sentence
import eu.dcotta.confis.model.Subject
import javax.swing.DefaultComboBoxModel
import kotlin.properties.Delegates

class SentenceComboBoxesModel {
    val subjectsModel = DefaultComboBoxModel<Subject>()
    val actionsModel = DefaultComboBoxModel<Action>()
    val objectsModel = DefaultComboBoxModel<Obj>()

    private inline fun <reified T> DefaultComboBoxModel<T>.setAll(all: Collection<T>) {
        val selected = this.selectedItem as? T
        removeAllElements()
        addAll(all)
        if (selected != null && selected in all) selectedItem = selected
    }

    private fun <T> refreshingObservable(default: T? = null) = Delegates.observable(default) { _, old, new ->
        if (old != new) refresh()
    }

    fun setSubjects(subjects: List<Subject>) = subjectsModel.setAll(subjects)
    var selectedSubject: Subject? by refreshingObservable()

    fun setActions(actions: List<Action>) = actionsModel.setAll(actions)
    var selectedAction: Action? by refreshingObservable()

    fun setObjects(objs: List<Obj>) = objectsModel.setAll(objs)
    var selectedObject: Obj? by refreshingObservable()

    val questionReady = AtomicBooleanProperty(false)
    val sentence = AtomicProperty<Sentence?>(null)

    private fun refresh() {
        sentence.set(sentence())
        questionReady.set(selectedSubject != null && selectedAction != null && selectedObject != null)
    }

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

    private fun <T> DefaultComboBoxModel<T>.getAll(): List<T> = (0 until size).map(::getElementAt)
}
