package eu.dcotta.confis.eval.inference

import eu.dcotta.confis.eval.ConfisRule
import eu.dcotta.confis.eval.askEngine
import eu.dcotta.confis.model.Agreement
import eu.dcotta.confis.model.Clause
import eu.dcotta.confis.model.Sentence
import eu.dcotta.confis.util.with
import kotlinx.collections.immutable.PersistentSet
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.persistentSetOf
import org.jeasy.rules.api.Facts
import org.jeasy.rules.api.Rules
import org.jeasy.rules.api.RulesEngineParameters
import org.jeasy.rules.core.InferenceRulesEngine
import org.jeasy.rules.core.RuleBuilder

/**
 * Question meant to represent _'Under what circumstances may A do X?'_
 */
@JvmInline
value class CircumstanceQuestion(val s: Sentence)

private class Builder(facts: Facts, q2: CircumstanceQuestion) : CircumstanceContext {

    override var circumstances: CircumstancesToClauses by facts with persistentMapOf()

    override var contradictions: PersistentSet<List<Clause>> by facts with persistentSetOf()

    override var unless: CircumstancesToClauses by facts with persistentMapOf()

    override val q by facts with q2
}

/**
 * Unlike allowance rules, a [CircumstanceRule] should
 * - **match when there are circumstances to add** to be able to perform the action
 * - **then add the new required circumstances** to the result
 */
data class CircumstanceRule(
    override val case: CircumstanceContext.() -> Boolean,
    override val then: CircumstanceContext.() -> Unit
): ConfisRule<CircumstanceContext>

fun Agreement.ask(q: CircumstanceQuestion): CircumstanceResult = askEngine(
    clauseToRule = { it.asCircumstanceRules() },
    buildContext = { fs -> Builder(fs, q) },
    rulesEngine = InferenceRulesEngine(),
    buildResult = { result ->
        when {
            result.contradictions.isNotEmpty() -> CircumstanceResult.Contradictory(result.contradictions)
            result.circumstances.isEmpty() && result.unless.isEmpty() -> CircumstanceResult.NotAllowed
            else -> CircumstanceResult.UnderCircumstances(result.circumstances.keys, result.unless.keys)
        }
    }
)
