package eu.dcotta.confis.eval

import eu.dcotta.confis.dsl.AgreementBuilder
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

data class QueryableAgreement(val agreement: Agreement, val defaultResult: AllowanceResult = Unspecified) {

    constructor(init: AgreementBuilder.() -> Unit) : this(AgreementBuilder(init))

    inner class Builder(private val facts: Facts) : RuleContext {
        override var result: AllowanceResult
            get() = facts.get(Companion.mutableResultKey)
                ?: error("fact should be present")
            set(value) {
                facts.put(Companion.mutableResultKey, value)
            }
        override val q: AllowanceQuestion
            get() = facts.get(questionKey)
                ?: error("fact should be present")
    }

    private val rs = (agreement.clauses zip agreement.clauses.flatMap { it.asRules() })
        .map { (clause, confisRule) ->
            RuleBuilder()
                .name(clause.toString())
                .`when` { fs -> confisRule.case(Builder(fs)) }
                .then { fs -> confisRule.then(Builder(fs)) }
                .build()
        }
        .toSet()
        .let(::Rules)

    fun ask(q: AllowanceQuestion): AllowanceResult {
        val facts = Facts().apply {
            put(mutableResultKey, defaultResult)
            put(questionKey, q)
        }
        DefaultRulesEngine().fire(rs, facts)

        return Builder(facts).result
    }

    companion object {
        private const val mutableResultKey = "mutableResult"
        private const val questionKey = "question"
    }
}
