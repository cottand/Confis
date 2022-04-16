package eu.dcotta.confis.eval.compliance

import eu.dcotta.confis.eval.ConfisRule
import eu.dcotta.confis.eval.askEngine
import eu.dcotta.confis.model.Agreement
import eu.dcotta.confis.model.CircumstanceMap
import org.jeasy.rules.api.Facts

fun Agreement.ask(q: ComplianceQuestion): ComplianceResult = askEngine(
    clauseToRule = ::asComplianceRules,
    buildContext = { Builder(it, q) },
    buildResult = { ctx -> TODO() },
)

/**
 * Question meant to represent _'Under what circumstances may A do X?'_
 */
@JvmInline
value class ComplianceQuestion(val cs: CircumstanceMap)

private class Builder(facts: Facts, q: ComplianceQuestion) : ComplianceContext {

}

sealed interface ComplianceResult

interface ComplianceContext {

}
class ComplianceRule(
    override val case: ComplianceContext.() -> Boolean,
    override val then: ComplianceContext.() -> Unit,
) : ConfisRule<ComplianceContext>
