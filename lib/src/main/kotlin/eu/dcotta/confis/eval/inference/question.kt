package eu.dcotta.confis.eval.inference

import eu.dcotta.confis.eval.CircumstanceQuestion
import eu.dcotta.confis.eval.ConfisRule
import eu.dcotta.confis.eval.askEngine
import eu.dcotta.confis.model.Agreement
import eu.dcotta.confis.model.Clause
import eu.dcotta.confis.util.with
import kotlinx.collections.immutable.PersistentSet
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.persistentSetOf
import org.jeasy.rules.api.Facts
import org.jeasy.rules.core.InferenceRulesEngine

class CircumstanceContext(facts: Facts, q2: CircumstanceQuestion) {

    var circumstances: CircumstancesToClauses by facts with persistentMapOf()

    var contradictions: PersistentSet<List<Clause>> by facts with persistentSetOf()

    var unless: CircumstancesToClauses by facts with persistentMapOf()

    val q by facts with q2
}

/**
 * Unlike allowance rules, a [CircumstanceRule] should
 * - **match when there are circumstances to add** to be able to perform the action
 * - **then add the new required circumstances** to the result
 */
data class CircumstanceRule(
    override val case: CircumstanceContext.() -> Boolean,
    override val then: CircumstanceContext.() -> Unit
) : ConfisRule<CircumstanceContext>

fun Agreement.ask(q: CircumstanceQuestion): CircumstanceResult = askEngine(
    clauseToRule = ::asCircumstanceRules,
    buildContext = { fs -> CircumstanceContext(fs, q) },
    rulesEngine = InferenceRulesEngine(),
    buildResult = { result ->
        when {
            result.contradictions.isNotEmpty() -> CircumstanceResult.Contradictory(result.contradictions)
            result.circumstances.isEmpty() && result.unless.isEmpty() -> CircumstanceResult.NotAllowed
            else -> CircumstanceResult.UnderCircumstances(result.circumstances.keys, result.unless.keys)
        }
    }
)
