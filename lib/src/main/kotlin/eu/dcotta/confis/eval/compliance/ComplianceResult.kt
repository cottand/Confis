package eu.dcotta.confis.eval.compliance

import eu.dcotta.confis.model.CircumstanceMap
import eu.dcotta.confis.model.Clause

sealed interface ComplianceResult {
    /**
     * Compliance is not possible
     */
    data class ComplianceImpossible(val clausesBreached: List<Clause>) : ComplianceResult

    /**
     * Compliant for now.
     * If [requirements] are fulfilled, then [FullyCompliant] status can be reached
     */
    data class CompliantIf(val requirements: Set<CircumstanceMap>) : ComplianceResult

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
