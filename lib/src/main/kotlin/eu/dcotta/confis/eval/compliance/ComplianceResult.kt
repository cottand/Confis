package eu.dcotta.confis.eval.compliance

import eu.dcotta.confis.model.Clause
import eu.dcotta.confis.model.circumstance.WorldState

sealed interface ComplianceResult {
    /**
     * Compliance is not possible - contract already breached
     */
    data class Breach(
        val clausesBreached: List<Clause>,
        val clausesPossiblyBreached: List<Clause> = emptyList(),
    ) : ComplianceResult

    data class PossibleBreach(
        val clausesPossiblyBreached: List<Clause>,
        val requirements: WorldState,
    ) : ComplianceResult

    /**
     * Compliant for now.
     * If [requirements] are fulfilled, then [FullyCompliant] status can be reached
     */
    data class CompliantIf(val requirements: WorldState) : ComplianceResult

    /**
     * Compliant, and further actions will not be needed to remain so
     *
     * Can stop being [FullyCompliant] if new actions are taken
     * (like breaching permission clauses)
     *
     * TODO do we want to replace this by a "can't breach anymore" result?
     */
    object FullyCompliant : ComplianceResult
}
