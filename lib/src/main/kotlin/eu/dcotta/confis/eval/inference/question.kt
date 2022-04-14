package eu.dcotta.confis.eval.inference

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

fun Agreement.ask(q: CircumstanceQuestion): CircumstanceResult {
    val rs = clauses.flatMap { c -> c.asCircumstanceRules().map { r -> c to r } }
        .mapIndexed { index, (clause, confisRule) ->
            RuleBuilder()
                .name("${clause::class.simpleName}#$index")
                .description(clause.toString())
                // rules have ordering as written in the contract - later -> higher priority (low number)
                .priority(-index)
                .`when` { fs -> confisRule.case(Builder(fs, q)) }
                .then { fs -> confisRule.then(Builder(fs, q)) }
                .build()
        }
        .toSet()
        .let(::Rules)

    val facts = Facts()

    val options = RulesEngineParameters().apply {
        isSkipOnFirstFailedRule = false
    }

    InferenceRulesEngine(options).fire(rs, facts)

    val result = Builder(facts, q)
    return when {
        result.contradictions.isNotEmpty() -> CircumstanceResult.Contradictory(result.contradictions)
        result.circumstances.isEmpty() && result.unless.isEmpty() -> CircumstanceResult.Forbidden
        else -> CircumstanceResult.UnderCircumstances(result.circumstances.keys, result.unless.keys)
    }
}
