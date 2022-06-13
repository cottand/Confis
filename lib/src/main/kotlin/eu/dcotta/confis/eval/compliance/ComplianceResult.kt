package eu.dcotta.confis.eval.compliance

import eu.dcotta.confis.eval.QueryResponse
import eu.dcotta.confis.model.Clause
import eu.dcotta.confis.model.circumstance.WorldState
import eu.dcotta.confis.model.circumstance.render
import eu.dcotta.confis.render.renderMd

sealed interface ComplianceResult : QueryResponse {
    /**
     * Compliance is not possible - contract already breached
     */
    data class Breach(
        val clausesBreached: List<Clause>,
        val clausesPossiblyBreached: List<Clause> = emptyList(),
    ) : ComplianceResult {
        override fun render() = "Breached the following clauses:\n" +
            clausesBreached
                .mapIndexed { index, clause -> clause.renderMd(index + 1).trimMargin() }
                .joinToString(separator = "") { "\n" + it }
    }

    data class PossibleBreach(
        val clausesPossiblyBreached: List<Clause>,
        val requirements: WorldState,
    ) : ComplianceResult {
        override fun render() = buildString {
            append("Compliant if then following happens:\n\n")
            append(requirements.render())
            append("\n\nPossibly breached:\n")
            clausesPossiblyBreached
                .mapIndexed { index, clause -> clause.renderMd(index + 1).trimMargin() }
                .forEach { append("\n$it") }
        }
    }

    /**
     * Compliant for now.
     * If [requirements] are fulfilled, then [FullyCompliant] status can be reached
     */
    data class CompliantIf(val requirements: WorldState) : ComplianceResult {
        override fun render() = "Compliant if the following happens:\n\n${requirements.render()}"
    }

    /**
     * Compliant, and further actions will not be needed to remain so
     *
     * Can stop being [FullyCompliant] if new actions are taken
     * (like breaching permission clauses)
     *
     * TODO do we want to replace this by a "can't breach anymore" result?
     */
    object FullyCompliant : ComplianceResult {
        override fun render() = "Fully compliant"
    }
}
