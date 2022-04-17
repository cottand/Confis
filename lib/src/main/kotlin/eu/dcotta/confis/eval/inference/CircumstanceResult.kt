package eu.dcotta.confis.eval.inference

import eu.dcotta.confis.model.CircumstanceMap
import eu.dcotta.confis.model.Clause

sealed interface CircumstanceResult {
    object NotAllowed : CircumstanceResult
    data class UnderCircumstances(

        /**
         * These are the circumstances unless which the question can be answered,
         * except the circumstances in [unless].
         */
        val circumstances: Set<CircumstanceMap>,

        /**
         * Under these circumstances, the sentence is _not [allowed][Allow]_. This does not
         * mean it is [forbidden][Forbid], just that the contract does not explicitly allow the sentence
         * under circumstances included inside [unless].
         */
        val unless: Set<CircumstanceMap> = emptySet(),
    ) : CircumstanceResult

    data class Contradictory(val contradictions: Set<List<Clause>>) : CircumstanceResult
}
