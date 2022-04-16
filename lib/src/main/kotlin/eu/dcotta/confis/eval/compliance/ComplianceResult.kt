package eu.dcotta.confis.eval.compliance

import eu.dcotta.confis.model.CircumstanceMap
import eu.dcotta.confis.model.Clause
import eu.dcotta.confis.model.Sentence

sealed interface ComplianceResult {
    /**
     * Compliance is not possible
     */
    data class ComplianceImpossible(val clausesBreached: List<Clause>) : ComplianceResult

    /**
     * Compliant for now.
     * If [requirements] are fulfilled, then [FullyCompliant] status can be reached
     */
    data class CompliantIf(val requirements: Map<Sentence, Set<CircumstanceMap>>) : ComplianceResult

    /**
     * Compliant, and further actions will ever be needed to remain so
     */
    object FullyCompliant : ComplianceResult
}
