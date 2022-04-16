package eu.dcotta.confis.eval.inference

import eu.dcotta.confis.model.Allowance.Allow
import eu.dcotta.confis.model.Allowance.Forbid
import eu.dcotta.confis.model.CircumstanceMap
import eu.dcotta.confis.model.Clause
import eu.dcotta.confis.model.Clause.Rule
import eu.dcotta.confis.model.Clause.SentenceWithCircumstances
import eu.dcotta.confis.model.Clause.Text
import kotlinx.collections.immutable.PersistentMap
import kotlinx.collections.immutable.plus

typealias CircumstancesToClauses = PersistentMap<CircumstanceMap, Clause>

fun asCircumstanceRules(clause: Clause): List<CircumstanceRule> = when (clause) {
    is Rule -> asCircumstanceRules(clause)
    is SentenceWithCircumstances -> asCircumstanceRules(clause)
    is Text -> emptyList()
}

fun asCircumstanceRules(r: SentenceWithCircumstances): List<CircumstanceRule> = when (r.rule.allowance) {
    Allow -> when (r.circumstanceAllowance) {
        // may .. asLongAs
        Allow -> listOf(
            CircumstanceRule(
                case = { r.rule.sentence generalises q.s && r.circumstances !in circumstances },
                then = { circumstances += (r.circumstances to r) },
            ),
        )
        // may .. unless
        Forbid -> listOf(
            // forbid when circumstances hold
            // contradiction detection
            CircumstanceRule(
                // if this forbid overlaps with any of the existing allowances from other rules
                case = {
                    r.sentence generalises q.s &&
                        circumstances.entries.any { it.key overlapsWith r.circumstances && it.value != r } &&
                        r !in contradictions.flatten()
                },
                then = {
                    val dissidents = circumstances.findClausesOverlappingWith(r.circumstances)
                    contradictions += setOf(dissidents + r)
                },
            ),
            // add to forbid set
            CircumstanceRule(
                case = { r.sentence generalises q.s && r.circumstances !in unless.keys },
                then = { unless += (r.circumstances to r) },
            ),
            // allow any other time
            CircumstanceRule(
                case = { r.sentence generalises q.s && CircumstanceMap.empty !in circumstances },
                then = { circumstances += (CircumstanceMap.empty to r) },
            ),
        )
    }
    Forbid -> when (r.circumstanceAllowance) {
        Allow -> listOf(
            // contradiction detection
            CircumstanceRule(
                case = {
                    // any of the allowed set overlap with this mayNot..asLongAs circumstances
                    r.sentence generalises q.s &&
                        circumstances.any { it.key overlapsWith r.circumstances } &&
                        r !in contradictions.flatten()
                },
                then = {
                    val culprits = circumstances.findClausesOverlappingWith(r.circumstances)
                    contradictions += setOf(culprits + r)
                },
            )
        )
        Forbid -> listOf(
            // contradiction detection
            CircumstanceRule(
                case = {
                    r.sentence generalises q.s &&
                        // every existing circumstance should be generalised by the exception clause
                        circumstances.any { !(r.circumstances generalises it.key) } &&
                        r !in contradictions.flatten()
                },
                then = {
                    val culprits = circumstances.filterNot { r.circumstances generalises it.key }.values
                    contradictions += setOf(culprits + r)
                },
            )
        )
    }
}

fun CircumstancesToClauses.findClausesOverlappingWith(others: CircumstanceMap): List<Clause> =
    filter { (circumstance, _) -> circumstance overlapsWith others }.values.toList()

private fun asCircumstanceRules(r: Rule) = when (r.allowance) {
    Allow -> listOf(
        // TODO contradiction detection?
        CircumstanceRule(
            case = { r.sentence generalises q.s && CircumstanceMap.empty !in circumstances },
            then = { circumstances += (CircumstanceMap.empty to r) },
        ),
    )
    Forbid -> listOf(
        // contradiction detection
        CircumstanceRule(
            case = { r.sentence generalises q.s && circumstances.isNotEmpty() && r !in contradictions.flatten() },
            then = {
                // find the other clauses that disagree with this one
                val dissidents = circumstances.values
                contradictions += setOf(dissidents + r)
            },
        ),
    )
}
