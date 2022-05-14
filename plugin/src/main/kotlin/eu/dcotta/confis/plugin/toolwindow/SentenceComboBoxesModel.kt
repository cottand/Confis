package eu.dcotta.confis.plugin.toolwindow

import eu.dcotta.confis.model.Action
import eu.dcotta.confis.model.Obj
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
}
