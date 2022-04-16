package eu.dcotta.confis.eval.allowance

import eu.dcotta.confis.eval.ConfisRule
import eu.dcotta.confis.model.Allowance.Allow
import eu.dcotta.confis.model.Allowance.Forbid
import eu.dcotta.confis.model.Clause
import eu.dcotta.confis.model.Clause.Rule
import eu.dcotta.confis.model.Clause.SentenceWithCircumstances
import eu.dcotta.confis.model.Clause.Text
import eu.dcotta.confis.model.computeAmbiguous

data class AllowanceRule(
    override val case: AllowanceContext.() -> Boolean,
    override val then: AllowanceContext.() -> Unit,
) : ConfisRule<AllowanceContext>

fun asAllowanceRules(clause: Clause): List<AllowanceRule> = when (clause) {
    is Rule -> asAllowanceRules(clause)
    is SentenceWithCircumstances -> asAllowanceRules(clause)
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
            // question general enough to concern us but not narrow enough to meet clause
            AllowanceRule(
                case = {
                    c.rule.sentence generalises q.sentence &&
                        !(c.circumstances generalises q.circumstances) &&
                        c.circumstances overlapsWith q.circumstances
                },
                then = { result = computeAmbiguous(result, Allow.asResult) },
            ),
        )
        // may - unless
        Forbid -> listOf(
            // specific case
            AllowanceRule(
                case = { c.rule.sentence generalises q.sentence && c.circumstances disjoint q.circumstances },
                then = { result = Allow.asResult },
            ),
            // question general enough to concern us but not narrow enough to meet clause
            AllowanceRule(
                case = {
                    c.rule.sentence generalises q.sentence &&
                        !(c.circumstances generalises q.circumstances) &&
                        c.circumstances overlapsWith q.circumstances
                },
                then = { result = computeAmbiguous(result, Forbid.asResult) },
            ),
        )
    }
    Forbid -> when (c.circumstanceAllowance) {
        Allow -> listOf(
            // specific case
            AllowanceRule(
                case = { c.rule.sentence generalises q.sentence && c.circumstances generalises q.circumstances },
                then = { result = Forbid.asResult }
            ),
            // question general enough to concern us but not narrow enough to meet clause
            AllowanceRule(
                case = {
                    c.rule.sentence generalises q.sentence &&
                        !(c.circumstances generalises q.circumstances) &&
                        c.circumstances overlapsWith q.circumstances
                },
                then = { result = computeAmbiguous(result, Forbid.asResult) },
            ),
        )
        Forbid -> listOf(
            // specific case
            // AllowanceRule(
            //    case = { c.rule.sentence.generalises(q.sentence) && c.circumstances.generalises(q.circumstances) },
            //    then = { result = Allow.asResult },
            // ),
            AllowanceRule(
                case = { c.rule.sentence generalises q.sentence && c.circumstances disjoint q.circumstances },
                then = { result = Forbid.asResult },
            ),
            // question general enough to concern us but not narrow enough to meet clause
            AllowanceRule(
                case = {
                    c.rule.sentence generalises q.sentence &&
                        !(c.circumstances generalises q.circumstances) &&
                        c.circumstances overlapsWith q.circumstances
                },
                then = { result = computeAmbiguous(result, Forbid.asResult) },
            ),
        )
    }
}
