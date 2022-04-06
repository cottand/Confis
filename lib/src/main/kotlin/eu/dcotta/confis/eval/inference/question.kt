package eu.dcotta.confis.eval.inference

import eu.dcotta.confis.model.Agreement
import eu.dcotta.confis.model.CircumstanceMap
import eu.dcotta.confis.model.Sentence
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

@JvmInline
private value class Builder(private val facts: Facts) : CircumstanceContext {
    override var result: CircumstanceMap
        get() = facts.get(mutableResultKey)
            ?: error("fact should be present")
        set(value) {
            facts.put(mutableResultKey, value)
        }
    override val q: CircumstanceQuestion
        get() = facts.get(questionKey)
            ?: error("fact should be present")
}

private const val mutableResultKey = "mutableResult"
private const val questionKey = "question"

fun Agreement.ask(q: CircumstanceQuestion): CircumstanceMap {
    val rs = clauses.flatMap { c -> c.asCircumstanceRules().map { r -> c to r } }
        .mapIndexed { index, (clause, confisRule) ->
            RuleBuilder()
                .name("${clause::class.simpleName}#$index")
                .description(clause.toString())
                .`when` { fs -> confisRule.case(Builder(fs)) }
                .then { fs -> confisRule.then(Builder(fs)) }
                .build()
        }
        .toSet()
        .let(::Rules)

    val facts = Facts().apply {
        put(mutableResultKey, CircumstanceMap.empty)
        put(questionKey, q)
    }

    val options = RulesEngineParameters().apply {
        isSkipOnFirstFailedRule = false
    }
    InferenceRulesEngine(options).fire(rs, facts)

    return Builder(facts).result
}
