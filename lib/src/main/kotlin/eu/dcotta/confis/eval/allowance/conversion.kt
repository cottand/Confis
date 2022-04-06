package eu.dcotta.confis.eval.allowance

import eu.dcotta.confis.model.Allowance.Allow
import eu.dcotta.confis.model.Allowance.Forbid
import eu.dcotta.confis.model.AllowanceResult
import eu.dcotta.confis.model.Clause
import eu.dcotta.confis.model.Clause.Rule
import eu.dcotta.confis.model.Clause.SentenceWithCircumstances
import eu.dcotta.confis.model.Clause.Text
import eu.dcotta.confis.model.computeAmbiguous
import eu.dcotta.confis.model.leastPermissive
import eu.dcotta.confis.model.mostPermissive

interface AllowanceContext {
    val q: AllowanceQuestion
    var result: AllowanceResult
}

data class AllowanceRule(val case: AllowanceContext.() -> Boolean, val then: AllowanceContext.() -> Unit)

fun Clause.asAllowanceRules(): List<AllowanceRule> = when (this) {
    is Rule -> asAllowanceRules(this)
    is SentenceWithCircumstances -> asAllowanceRules(this)
    is Text -> emptyList()
}

// TODO revise if these should really be the semantics but it looks alright
private fun asAllowanceRules(r: Rule): List<AllowanceRule> = listOf(
    AllowanceRule(
        case = { r.sentence generalises q.sentence },
        then = { result = r.allowance.asResult }
    )
)

private fun asAllowanceRules(c: SentenceWithCircumstances): List<AllowanceRule> = when (c.rule.allowance) {
    Allow -> when (c.circumstanceAllowance) {
        // allow asLongAs:
        Allow -> listOf(
            // !C -> A unspecified
            // C -> A allowed
            AllowanceRule(
                case = { c.rule.sentence generalises q.sentence && c.circumstances generalises q.circumstances },
                then = { result = Allow.asResult },
            ),
            // question too general case
            AllowanceRule(
                case = { c.rule.sentence.generalises(q.sentence) && q.circumstances generalises c.circumstances },
                then = { result = computeAmbiguous(result, Allow.asResult) },
            ),
        )
        // may - unless
        Forbid -> listOf(
            // specific case
            AllowanceRule(
                case = { c.rule.sentence.generalises(q.sentence) && c.circumstances.generalises(q.circumstances) },
                then = { result = Forbid.asResult },
            ),
            AllowanceRule(
                case = { c.rule.sentence generalises q.sentence && c.circumstances disjoint q.circumstances },
                // allows only if no one else forbid
                then = { result = result leastPermissive Allow.asResult },
            ),
            // question too general case
            AllowanceRule(
                case = { c.rule.sentence.generalises(q.sentence) && q.circumstances generalises c.circumstances },
                then = { result = computeAmbiguous(result, Forbid.asResult) },
            ),
        )
    }
    Forbid -> when (c.circumstanceAllowance) {
        Allow -> listOf(
            AllowanceRule(
                case = { c.rule.sentence generalises q.sentence && c.circumstances generalises q.circumstances },
                then = { result = Forbid.asResult }
            ),
            // question too general case
            AllowanceRule(
                case = { c.rule.sentence.generalises(q.sentence) && q.circumstances generalises c.circumstances },
                then = { result = computeAmbiguous(result, Forbid.asResult) },
            ),
        )
        Forbid -> listOf(
            // specific case
            AllowanceRule(
                case = { c.rule.sentence.generalises(q.sentence) && c.circumstances.generalises(q.circumstances) },
                then = { result = Allow.asResult },
            ),
            AllowanceRule(
                case = { c.rule.sentence generalises q.sentence && c.circumstances disjoint q.circumstances },
                then = { result = mostPermissive(result, Forbid.asResult) },
            ),
            // we made an exception to a generality, so let's make clear more general questions cannot be answered
            AllowanceRule(
                case = { c.rule.sentence.generalises(q.sentence) && q.circumstances generalises c.circumstances },
                then = { result = computeAmbiguous(result, Allow.asResult) },
            ),
        )
    }
}
