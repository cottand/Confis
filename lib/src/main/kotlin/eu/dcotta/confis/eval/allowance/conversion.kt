package eu.dcotta.confis.eval.allowance

import eu.dcotta.confis.eval.ConfisRule
import eu.dcotta.confis.model.Allowance.Allow
import eu.dcotta.confis.model.Allowance.Forbid
import eu.dcotta.confis.model.Clause
import eu.dcotta.confis.model.Clause.Permission
import eu.dcotta.confis.model.Clause.PermissionWithCircumstances
import eu.dcotta.confis.model.Clause.Requirement
import eu.dcotta.confis.model.Clause.RequirementWithCircumstances
import eu.dcotta.confis.model.Clause.Text
import eu.dcotta.confis.model.computeAmbiguous

data class AllowanceRule(
    override val case: AllowanceContext.() -> Boolean,
    override val then: AllowanceContext.() -> Unit,
) : ConfisRule<AllowanceContext>

fun asAllowanceRules(clause: Clause): List<AllowanceRule> = when (clause) {
    is Permission -> asAllowanceRules(clause)
    is PermissionWithCircumstances -> asAllowanceRules(clause)
    is Text -> emptyList()
    // as far as allowance is concerned, requirement clauses are like permission clauses
    is Requirement -> asAllowanceRules(Permission(Allow, clause.sentence))
    is RequirementWithCircumstances ->
        asAllowanceRules(PermissionWithCircumstances(Permission(Allow, clause.sentence), Allow, clause.circumstances))
}

// TODO revise if these should really be the semantics but it looks alright
private fun asAllowanceRules(r: Permission): List<AllowanceRule> = listOf(
    AllowanceRule(
        case = { r.sentence generalises q.sentence },
        then = { result = r.allowance.asResult }
    )
)

private fun asAllowanceRules(c: PermissionWithCircumstances): List<AllowanceRule> = when (c.permission.allowance) {
    Allow -> when (c.circumstanceAllowance) {
        // allow asLongAs:
        Allow -> listOf(
            // !C -> A unspecified
            // C -> A allowed
            AllowanceRule(
                case = { c.permission.sentence generalises q.sentence && c.circumstances generalises q.circumstances },
                then = { result = Allow.asResult },
            ),
            // question general enough to concern us but not narrow enough to meet clause
            AllowanceRule(
                case = {
                    c.permission.sentence generalises q.sentence &&
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
                case = { c.permission.sentence generalises q.sentence && c.circumstances disjoint q.circumstances },
                then = { result = Allow.asResult },
            ),
            // question general enough to concern us but not narrow enough to meet clause
            AllowanceRule(
                case = {
                    c.permission.sentence generalises q.sentence &&
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
                case = { c.permission.sentence generalises q.sentence && c.circumstances generalises q.circumstances },
                then = { result = Forbid.asResult }
            ),
            // question general enough to concern us but not narrow enough to meet clause
            AllowanceRule(
                case = {
                    c.permission.sentence generalises q.sentence &&
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
                case = { c.permission.sentence generalises q.sentence && c.circumstances disjoint q.circumstances },
                then = { result = Forbid.asResult },
            ),
            // question general enough to concern us but not narrow enough to meet clause
            AllowanceRule(
                case = {
                    c.permission.sentence generalises q.sentence &&
                        !(c.circumstances generalises q.circumstances) &&
                        c.circumstances overlapsWith q.circumstances
                },
                then = { result = computeAmbiguous(result, Forbid.asResult) },
            ),
        )
    }
}
