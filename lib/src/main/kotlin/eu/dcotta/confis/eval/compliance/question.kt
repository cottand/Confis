package eu.dcotta.confis.eval.compliance

import eu.dcotta.confis.eval.ComplianceQuestion
import eu.dcotta.confis.eval.ConfisRule
import eu.dcotta.confis.eval.askEngine
import eu.dcotta.confis.model.Agreement
import eu.dcotta.confis.model.Clause
import eu.dcotta.confis.model.circumstance.WorldState
import eu.dcotta.confis.util.with
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentHashMapOf
import kotlinx.collections.immutable.persistentListOf
import org.jeasy.rules.api.Facts

fun Agreement.ask(q: ComplianceQuestion): ComplianceResult = askEngine(
    clauseToRule = ::asComplianceRules,
    buildContext = { ComplianceContext(it, q) },
    buildResult = { ctx ->
        when {
            ctx.breached.isNotEmpty() -> ComplianceResult.Breach(ctx.breached, ctx.possiblyBreached)
            ctx.possiblyBreached.isNotEmpty() -> ComplianceResult.PossibleBreach(ctx.possiblyBreached, ctx.required)
            ctx.required.isNotEmpty() -> ComplianceResult.CompliantIf(ctx.required)
            else -> ComplianceResult.FullyCompliant
        }
    },
)

internal class ComplianceContext(facts: Facts, q2: ComplianceQuestion) {
    val q by facts with q2
    var required: WorldState by facts with persistentHashMapOf()
    var breached: PersistentList<Clause> by facts with persistentListOf()
    var possiblyBreached: PersistentList<Clause> by facts with persistentListOf()
}

internal class ComplianceRule(
    override val case: ComplianceContext.() -> Boolean,
    override val then: ComplianceContext.() -> Unit,
) : ConfisRule<ComplianceContext>
