package eu.dcotta.confis.eval.compliance

import eu.dcotta.confis.model.Allowance.Allow
import eu.dcotta.confis.model.Allowance.Forbid
import eu.dcotta.confis.model.Clause
import eu.dcotta.confis.model.Clause.Permission
import eu.dcotta.confis.model.Clause.PermissionWithCircumstances
import eu.dcotta.confis.model.Clause.Requirement
import eu.dcotta.confis.model.Clause.RequirementWithCircumstances
import eu.dcotta.confis.model.Clause.Text
import eu.dcotta.confis.model.PastAction
import eu.dcotta.confis.model.PrecedentSentence
import eu.dcotta.confis.util.plus
import kotlinx.collections.immutable.plus

internal fun asComplianceRules(c: Clause): List<ComplianceRule> = when (c) {
    is Text -> emptyList()
    is Requirement -> asComplianceRules(c)
    is RequirementWithCircumstances -> TODO()
    is Permission -> asComplianceRules(c)
    is PermissionWithCircumstances -> asComplianceRules(c)
}

private fun asComplianceRules(c: Requirement): List<ComplianceRule> = listOf(
    // if requirement not already present in current circumstances
    ComplianceRule(
        case = { !(c.sentence happenedIn q.state) },
        then = { required += PrecedentSentence(c.sentence) },
    )
)

private fun asComplianceRules(c: Permission): List<ComplianceRule> = when (c.allowance) {
    Allow -> emptyList()
    Forbid -> listOf(
        ComplianceRule(
            case = { c.sentence happenedIn q.state },
            then = { breached += c },
        )
    )
}

private fun asComplianceRules(c: PermissionWithCircumstances): List<ComplianceRule> {
    val pastAction by lazy { PastAction(c.sentence, c.circumstances) }

    return when (c.permission.allowance) {
        Allow -> emptyList()
        Forbid -> when (c.circumstanceAllowance) {
            // mayNot..asLongAs
            Allow -> listOf(
                ComplianceRule(
                    case = { pastAction happenedIn q.state },
                    then = { breached += c }
                ),
                // possibly a breach, but the past action is too general to be sure it is compliant
                ComplianceRule(
                    case = {
                        !(pastAction happenedIn q.state) && pastAction possiblyHappenedIn q.state
                    },
                    then = { possiblyBreached += c }
                ),
            )
            // mayNot..unless
            Forbid -> listOf(
                // action did happen and it was outside of the exception -> breach
                ComplianceRule(
                    case = { c.sentence happenedIn q.state && !(pastAction happenedIn q.state) },
                    then = { breached += c },
                ),
            )
        }
    }
}
