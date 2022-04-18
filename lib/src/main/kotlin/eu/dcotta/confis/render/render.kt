package eu.dcotta.confis.render

import eu.dcotta.confis.model.Action
import eu.dcotta.confis.model.Agreement
import eu.dcotta.confis.model.Allowance
import eu.dcotta.confis.model.Allowance.Allow
import eu.dcotta.confis.model.Allowance.Forbid
import eu.dcotta.confis.model.CircumstanceMap
import eu.dcotta.confis.model.Clause
import eu.dcotta.confis.model.Clause.Permission
import eu.dcotta.confis.model.Clause.PermissionWithCircumstances
import eu.dcotta.confis.model.Clause.Requirement
import eu.dcotta.confis.model.Clause.RequirementWithCircumstances
import eu.dcotta.confis.model.Clause.Text
import eu.dcotta.confis.model.Party
import org.intellij.lang.annotations.Language

@Language("markdown")
fun Agreement.renderMarkdown(): String {

    val parties = parties
        .mapIndexed { index, party -> party.renderMdLine(index + 1) }
        .joinToString(separator = "\n")

    val actions = actions
        .mapIndexed { index, action -> action.renderMdLine(index + 1) }
        .joinToString(separator = "\n")

    val clauses = clauses
        .mapIndexed { index, clause -> clause.renderMd(index + 1) }
        .joinToString(separator = "\n")
    return """
        |# ${title ?: "Confis Agreement"}
        |
        |${introduction?.replace("\n", "|\n") ?: ""}
        |
        |## 1 - Parties
        |
        $parties
        |        
        |## 2 - Definitions
        |
        $actions
        |
        |## 3 - Terms
        |
        $clauses
        |
    """.trimMargin()
}

@Language("markdown")
private fun Party.renderMdLine(index: Int): String = when {
    description != null -> "|$index. $description (**$name**)"
    else -> "|$index. **$name**"
}

@Language("markdown")
private fun Action.renderMdLine(index: Int): String = when {
    description != null -> """|$index. _"$name"_: $description"""
    else -> """|$index. _"$name"_"""
}

@Language("markdown")
fun Clause.renderMd(index: Int): String {
    fun Allowance.clausePermission() = when (this) {
        Allow -> "may"
        Forbid -> "may not"
    }
    fun Allowance.circumstancePermission() = when (this) {
        Allow -> "under the following circumstances"
        Forbid -> "except under the following circumstances"
    }
    return when (this) {
        is Permission -> """
        |$index. ${sentence.subject.render()} ${allowance.clausePermission()} ${action.render()} ${obj.render()}"""
        is PermissionWithCircumstances -> permission.renderMd(index) + " ${circumstanceAllowance
            .circumstancePermission()}:" + circumstances.renderMdList()
        is Requirement -> TODO()
        is RequirementWithCircumstances -> TODO()
        is Text -> "|$index. ${string.replace("\n", "|\n")}"
    }.trimIndent()
}

private fun CircumstanceMap.renderMdList(): String =
    toList()
        .mapIndexed { index, circumstance -> "\n|  ${index + 1}. " + circumstance.render() }
        .joinToString(separator = "")
