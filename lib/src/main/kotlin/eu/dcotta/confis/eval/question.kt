package eu.dcotta.confis.eval

import eu.dcotta.confis.model.Agreement
import eu.dcotta.confis.model.AllowanceResult
import eu.dcotta.confis.model.AllowanceResult.Unspecified
import eu.dcotta.confis.model.CircumstanceMap
import eu.dcotta.confis.model.Purpose
import eu.dcotta.confis.model.PurposePolicy
import eu.dcotta.confis.model.Sentence
import org.jeasy.rules.api.Facts
import org.jeasy.rules.api.Rules
import org.jeasy.rules.core.DefaultRulesEngine
import org.jeasy.rules.core.RuleBuilder

data class AllowanceQuestion(
    val sentence: Sentence,
    val circumstances: CircumstanceMap = CircumstanceMap.empty,
) {
    constructor(sentence: Sentence, purpose: Purpose) :
        this(sentence, CircumstanceMap.of(PurposePolicy(purpose)))
}

@JvmInline
private value class Builder(private val facts: Facts) : RuleContext {
    override var result: AllowanceResult
        get() = facts.get(mutableResultKey)
            ?: error("fact should be present")
        set(value) {
            facts.put(mutableResultKey, value)
        }
    override val q: AllowanceQuestion
        get() = facts.get(questionKey)
            ?: error("fact should be present")
}

private const val mutableResultKey = "mutableResult"
private const val questionKey = "question"
fun Agreement.ask(q: AllowanceQuestion, defaultResult: AllowanceResult = Unspecified): AllowanceResult {
    val rs = clauses.flatMap { c -> c.asAllowanceRules().map { r -> c to r } }
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
        put(mutableResultKey, defaultResult)
        put(questionKey, q)
    }
    DefaultRulesEngine().fire(rs, facts)

    return Builder(facts).result
}
