package eu.dcotta.confis.eval.inference

import eu.dcotta.confis.eval.QueryResponse
import eu.dcotta.confis.model.CircumstanceMap
import eu.dcotta.confis.model.Clause
import eu.dcotta.confis.render.renderMd

sealed interface CircumstanceResult : QueryResponse {
    object NotAllowed : CircumstanceResult {
        override fun render() = "Not allowed"
    }

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
    ) : CircumstanceResult {
        override fun render() = buildString {
            append("Under circumstances:\n")
            append("  - ${circumstances.joinToString(separator = "  \n") { it.render() }}\n")
            append(
                if (unless.isNotEmpty())
                    "Not explicitly allowed when:\n  - ${unless.joinToString(separator = "  \n") { it.render() }}"
                else
                    ""
            )
        }
    }

    data class Contradictory(val contradictions: Set<List<Clause>>) : CircumstanceResult {
        override fun render() = "Contradictions found in circumstances. The following clauses are contradictory:\n  " +
            contradictions.joinToString(separator = "  \n") {
                it.mapIndexed { index, clause -> clause.renderMd(index + 1).trimMargin() }
                    .joinToString(prefix = "- The clause:\n", separator = "\n\n- The clause\n", postfix = ";\n\n")
            }
    }
}
