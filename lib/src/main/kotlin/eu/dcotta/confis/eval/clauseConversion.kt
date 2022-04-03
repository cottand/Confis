package eu.dcotta.confis.eval

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

interface RuleContext {
    val q: AllowanceQuestion
    var result: AllowanceResult
}

data class ConfisRule(val case: RuleContext.() -> Boolean, val then: RuleContext.() -> Unit)

fun Clause.asRules(): List<ConfisRule> = when (this) {
    is Rule -> asRules(this)
    is SentenceWithCircumstances -> asRules(this)
    is Text -> emptyList()
}

// TODO revise if these should really be the semantics but it looks alright
fun asRules(r: Rule): List<ConfisRule> = listOf(
    ConfisRule(
        case = { r.sentence generalises q.sentence },
        then = { result = r.allowance.asResult }
    )
)

fun asRules(c: SentenceWithCircumstances): List<ConfisRule> = when (c.rule.allowance) {
    Allow -> when (c.circumstanceAllowance) {
        // allow asLongAs:
        Allow -> listOf(
            // !C -> A unspecified
            // C -> A allowed
            ConfisRule(
                case = { c.rule.sentence generalises q.sentence && c.circumstances generalises q.circumstances },
                then = { result = Allow.asResult },
            ),
            // question too general case
            ConfisRule(
                case = { c.rule.sentence.generalises(q.sentence) && q.circumstances generalises c.circumstances },
                then = { result = computeAmbiguous(result, Allow.asResult) },
            ),
        )
        // may - unless
        Forbid -> listOf(
            // specific case
            ConfisRule(
                case = { c.rule.sentence.generalises(q.sentence) && c.circumstances.generalises(q.circumstances) },
                then = { result = Forbid.asResult },
            ),
            ConfisRule(
                case = { c.rule.sentence generalises q.sentence && c.circumstances disjoint q.circumstances },
                // allows only if no one else forbid
                then = { result = result leastPermissive Allow.asResult },
            ),
            // question too general case
            ConfisRule(
                case = { c.rule.sentence.generalises(q.sentence) && q.circumstances generalises c.circumstances },
                then = { result = computeAmbiguous(result, Forbid.asResult) },
            ),
        )
    }
    Forbid -> when (c.circumstanceAllowance) {
        Allow -> listOf(
            ConfisRule(
                case = { c.rule.sentence generalises q.sentence && c.circumstances generalises q.circumstances },
                then = { result = Forbid.asResult }
            ),
            // question too general case
            ConfisRule(
                case = { c.rule.sentence.generalises(q.sentence) && q.circumstances generalises c.circumstances },
                then = { result = computeAmbiguous(result, Forbid.asResult) },
            ),
        )
        Forbid -> listOf(
            // specific case
            ConfisRule(
                case = { c.rule.sentence.generalises(q.sentence) && c.circumstances.generalises(q.circumstances) },
                then = { result = Allow.asResult },
            ),
            ConfisRule(
                case = { c.rule.sentence generalises q.sentence && c.circumstances disjoint q.circumstances },
                then = { result = mostPermissive(result, Forbid.asResult) },
            ),
            // we made an exception to a generality, so let's make clear more general questions cannot be answered
            ConfisRule(
                case = { c.rule.sentence.generalises(q.sentence) && q.circumstances generalises c.circumstances },
                then = { result = computeAmbiguous(result, Allow.asResult) },
            ),
        )
    }
}
