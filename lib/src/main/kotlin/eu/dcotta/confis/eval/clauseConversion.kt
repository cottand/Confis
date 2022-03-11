package eu.dcotta.confis.eval

import eu.dcotta.confis.model.Allowance.Allow
import eu.dcotta.confis.model.Allowance.Forbid
import eu.dcotta.confis.model.AllowanceResult
import eu.dcotta.confis.model.Clause
import eu.dcotta.confis.model.Clause.Rule
import eu.dcotta.confis.model.Clause.SentenceWithCircumstances
import eu.dcotta.confis.model.Clause.Text

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
        case = { r.sentence in q.sentence },
        then = { result = r.allowance.asResult }
    )
)

fun asRules(c: SentenceWithCircumstances): List<ConfisRule> = when (c.rule.allowance) {
    Allow -> when (c.circumstanceAllowance) {
        // C -> S
        Allow -> listOf(
            ConfisRule(
                case = { c.rule.sentence in q.sentence && c.circumstances in q.circumstances },
                then = { result = Allow.asResult },
            )
        )
        // may - unless
        Forbid -> listOf(
            ConfisRule(
                case = { c.rule.sentence in q.sentence && c.circumstances in q.circumstances },
                then = { result = Forbid.asResult },
            ),
            ConfisRule(
                case = { c.rule.sentence in q.sentence && c.circumstances disjoint q.circumstances },
                // allows only if no one else forbid
                then = { result = result and Allow.asResult },
            ),
        )
    }
    Forbid -> when (c.circumstanceAllowance) {
        Allow -> listOf(
            ConfisRule(
                case = { c.rule.sentence in q.sentence && c.circumstances in q.circumstances },
                then = { result = Forbid.asResult }
            )
        )
        Forbid -> listOf(
            ConfisRule(
                case = { c.rule.sentence in q.sentence && c.circumstances in q.circumstances },
                then = { result = Allow.asResult },
            )
        )
    }
}
