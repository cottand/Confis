package eu.dcotta.confis.render

import eu.dcotta.confis.model.Action
import eu.dcotta.confis.model.Agreement
import eu.dcotta.confis.model.Party
import org.intellij.lang.annotations.Language

@Language("markdown")
fun Agreement.renderMarkdown(): String {

    val parties = parties
        .mapIndexed { index, party -> party.renderMdLine(1, index + 1) }
        .joinToString(separator = "\n")

    val actions = actions
        .mapIndexed { index, action -> action.renderMdLine(2, index + 1) }
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
    """.trimMargin()
}

@Language("markdown")
private fun Party.renderMdLine(section: Int, index: Int): String = when {
    description != null -> "|- $section.$index $description (**$name**)"
    else -> "|- $section.$index **$name**"
}

@Language("markdown")
private fun Action.renderMdLine(section: Int, index: Int): String = when {
    description != null -> """|- $section.$index _"$name"_ : $description"""
    else -> """|- $section.$index _"$name"_"""
}
