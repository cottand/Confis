package eu.dcotta.confis.eval.inference

import eu.dcotta.confis.model.Allowance.Allow
import eu.dcotta.confis.model.Allowance.Forbid
import eu.dcotta.confis.model.CircumstanceMap
import eu.dcotta.confis.model.Clause
import eu.dcotta.confis.model.Clause.Rule
import eu.dcotta.confis.model.Clause.SentenceWithCircumstances
import eu.dcotta.confis.model.Clause.Text

sealed interface CircumstanceResult {
    object Forbidden : CircumstanceResult
    data class UnderCircumstances(val circumstances: Set<CircumstanceMap>) : CircumstanceResult
    data class Contradictory(val contradictions: Set<List<Clause>>) : CircumstanceResult
}

typealias CircumstancesToClauses = Map<CircumstanceMap, MutableList<out Clause>>

interface CircumstanceContext {
    val q: CircumstanceQuestion
    var circumstances: CircumstancesToClauses
    var contradictions: Set<List<Clause>>
}

/**
 * Unlike allowance rules, a [CircumstanceRule] should
 * - **match when there are circumstances to add** to be able to perform the action
 * - **then add the new required circumstances** to the result
 */
data class CircumstanceRule(val case: CircumstanceContext.() -> Boolean, val then: CircumstanceContext.() -> Unit)

fun Clause.asCircumstanceRules(): List<CircumstanceRule> =
    when (this) {
        is Rule -> asCircumstanceRules(this)
        is SentenceWithCircumstances -> asCircumstanceRules(this)
        is Text -> emptyList()
    }

fun asCircumstanceRules(r: SentenceWithCircumstances): List<CircumstanceRule> = when (r.rule.allowance) {
    Allow -> when (r.circumstanceAllowance) {
        // may .. asLongAs
        Allow -> TODO()
        Forbid -> TODO()
    }
    Forbid -> TODO()
}

// FIXME - if allowance is no the should we take away circumstances? Or detect contradictions!?
private fun asCircumstanceRules(r: Rule) = when (r.allowance) {
    Allow -> listOf(
        // TODO contradiction detection?
        CircumstanceRule(
            case = { r.sentence generalises q.s && CircumstanceMap.empty !in circumstances },
            then = { circumstances += (CircumstanceMap.empty to mutableListOf(r)) },
        ),
    )
    Forbid -> listOf(
        // contradiction detection
        CircumstanceRule(
            case = { r.sentence generalises q.s && circumstances.isNotEmpty() && r !in contradictions.flatten() },
            then = {
                // find the other clauses that disagree with this one
                val dissidents = circumstances.values.flatten()
                contradictions = contradictions + setOf(dissidents + r)
            },
        ),
    )
}
