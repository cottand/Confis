package eu.dcotta.confis.eval.compliance

import eu.dcotta.confis.eval.ConfisRule
import eu.dcotta.confis.eval.askEngine
import eu.dcotta.confis.model.Agreement
import eu.dcotta.confis.model.Circumstance
import eu.dcotta.confis.model.CircumstanceMap
import eu.dcotta.confis.model.Clause
import eu.dcotta.confis.util.with
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.PersistentSet
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentSetOf
import org.jeasy.rules.api.Facts

fun Agreement.ask(q: ComplianceQuestion): ComplianceResult = askEngine(
    clauseToRule = ::asComplianceRules,
    buildContext = { ComplianceContext(it, q) },
    buildResult = { ctx ->
        when {
            ctx.breached.isNotEmpty() -> ComplianceResult.ComplianceImpossible(ctx.breached)
            ctx.required.isNotEmpty() -> ComplianceResult.CompliantIf(ctx.required)
            else -> ComplianceResult.FullyCompliant
        }
    },
)

/**
 * Question meant to represent _'What do I need to do in order to comply to the agreement?'_
 */
@JvmInline
value class ComplianceQuestion(val cs: CircumstanceMap) {
    constructor(vararg circumstances: Circumstance) : this(CircumstanceMap.of(*circumstances))
}

internal class ComplianceContext(facts: Facts, q2: ComplianceQuestion) {
    val q by facts with q2
    var required: PersistentSet<CircumstanceMap> by facts with persistentSetOf()
    var breached: PersistentList<Clause> by facts with persistentListOf()
}

internal class ComplianceRule(
    override val case: ComplianceContext.() -> Boolean,
    override val then: ComplianceContext.() -> Unit,
) : ConfisRule<ComplianceContext>
