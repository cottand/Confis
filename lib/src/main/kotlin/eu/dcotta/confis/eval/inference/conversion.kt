package eu.dcotta.confis.eval.inference

import eu.dcotta.confis.model.Allowance.Allow
import eu.dcotta.confis.model.Allowance.Forbid
import eu.dcotta.confis.model.CircumstanceMap
import eu.dcotta.confis.model.Clause
import eu.dcotta.confis.model.Clause.Rule
import eu.dcotta.confis.model.Clause.SentenceWithCircumstances
import eu.dcotta.confis.model.Clause.Text

interface CircumstanceContext {
    val q: CircumstanceQuestion
    var result: CircumstanceMap
}


/**
 * Unlike allowance rules, a [CircumstanceRule] should
 * - **match when there are circumstances to add** to be able to perform the action
 * - **then add the new required circumstances** to the result
 */
data class CircumstanceRule(val case: CircumstanceContext.() -> Boolean, val then: CircumstanceContext.() -> Unit)

fun Clause.asCircumstanceRules(): List<CircumstanceRule> =
    when(this) {
        is Rule -> asCircumstanceRules(this)
        is SentenceWithCircumstances -> asCircumstanceRules(this)
        is Text -> emptyList()
    }

fun asCircumstanceRules(r: SentenceWithCircumstances): List<CircumstanceRule> = when(r.rule.allowance) {
    Allow -> when(r.circumstanceAllowance) {
        // may .. asLongAs
        Allow -> TODO()
        Forbid -> TODO()
    }
    Forbid -> TODO()
}

private fun asCircumstanceRules(r: Rule) = listOf(
    CircumstanceRule(
        case = { false },
        then = { /* do nothing */ }
    )
)
