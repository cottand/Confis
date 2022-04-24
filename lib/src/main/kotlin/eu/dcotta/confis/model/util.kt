package eu.dcotta.confis.model

import eu.dcotta.confis.model.Clause.Permission
import eu.dcotta.confis.model.Clause.PermissionWithCircumstances
import eu.dcotta.confis.model.Clause.Requirement
import eu.dcotta.confis.model.Clause.RequirementWithCircumstances
import eu.dcotta.confis.model.Clause.Text

internal fun Clause.extractActions(): List<Action> {

    fun CircumstanceMap.extractActions() = get(PrecedentSentence.KeySet).map { it.sentence.action }

    val actionInClause = when (this) {
        is Permission -> action
        is PermissionWithCircumstances -> sentence.action
        is Requirement -> action
        is RequirementWithCircumstances -> sentence.action
        is Text -> null
    }

    val actionsInCircumstances = when (this) {
        is NoCircumstance -> emptyList()
        is PermissionWithCircumstances -> circumstances.extractActions()
        is RequirementWithCircumstances -> circumstances.extractActions()
    }

    return actionInClause?.let { listOf(it) }.orEmpty() + actionsInCircumstances
}

internal fun Clause.extractObjs(): List<Obj> {

    fun CircumstanceMap.extractObjs() = get(PrecedentSentence.KeySet).map { it.sentence.obj }

    val objsInClause = when (this) {
        is Permission -> obj
        is PermissionWithCircumstances -> sentence.obj
        is Requirement -> obj
        is RequirementWithCircumstances -> sentence.obj
        is Text -> null
    }

    val objsInCircumstances = when (this) {
        is NoCircumstance -> emptyList()
        is PermissionWithCircumstances -> circumstances.extractObjs()
        is RequirementWithCircumstances -> circumstances.extractObjs()
    }

    return (objsInClause?.let { listOf(it) }.orEmpty() + objsInCircumstances).filter { it != Obj.Anything }
}
