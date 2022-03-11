package eu.dcotta.confis.eval

import com.deliveredtechnologies.rulebook.Fact
import com.deliveredtechnologies.rulebook.FactMap
import com.deliveredtechnologies.rulebook.Result
import com.deliveredtechnologies.rulebook.lang.RuleBookBuilder
import com.deliveredtechnologies.rulebook.lang.RuleBuilder
import eu.dcotta.confis.dsl.AgreementBuilder
import eu.dcotta.confis.model.Agreement
import eu.dcotta.confis.model.Allowance.Allow
import eu.dcotta.confis.model.Allowance.Forbid
import eu.dcotta.confis.model.AllowanceResult
import eu.dcotta.confis.model.CircumstanceMap
import eu.dcotta.confis.model.Clause
import eu.dcotta.confis.model.Clause.Rule
import eu.dcotta.confis.model.Clause.SentenceWithCircumstances
import eu.dcotta.confis.model.Clause.Text
import eu.dcotta.confis.model.Purpose
import eu.dcotta.confis.model.PurposePolicy
import eu.dcotta.confis.model.Sentence

data class AllowanceQuestion(
    val sentence: Sentence,
    val circumstances: CircumstanceMap = CircumstanceMap.empty,
) {
    constructor(sentence: Sentence, purpose: Purpose) :
        this(sentence, CircumstanceMap.of(PurposePolicy(purpose)))
}

data class FactAndQuestion(val q: AllowanceQuestion)

data class QueryableAgreement(val agreement: Agreement) {

    constructor(init: AgreementBuilder.() -> Unit) : this(AgreementBuilder(init))

    private val rb = RuleBookBuilder.create()
        .withResultType(AllowanceResult::class.java)
        .withDefaultResult(AllowanceResult.Unspecified)
        .build()

    init {
        for ((clause, confisRule) in agreement.clauses zip agreement.clauses.flatMap { it.asRules() })
            rb.addRule(
                RuleBuilder.create()
                    .withName(clause.toString())
                    .withFactType(FactAndQuestion::class.java)
                    .withResultType(AllowanceResult::class.java)
                    .`when` { confisRule.case(it.one) }
                    .then { facts, result -> confisRule.then(ThenBuilder(result, facts.one)) }
                    .build()
            )
    }

    fun ask(q: AllowanceQuestion): AllowanceResult {
        rb.run(FactMap(Fact(FactAndQuestion(q))))
        return rb.result.orElse(null)?.value ?: error("Should contain a result")
    }

    private class ThenBuilder(
        private val allowanceResult: Result<AllowanceResult>,
        var factAndQuestion: FactAndQuestion,
    ) {
        var result: AllowanceResult
            get() = allowanceResult.value
            set(value) {
                allowanceResult.value = value
            }
    }

    private data class ConfisRule(val case: FactAndQuestion.() -> Boolean, val then: ThenBuilder.() -> Unit)

    private fun Clause.asRules(): List<ConfisRule> = when (this) {
        is Rule -> asRules(this)
        is SentenceWithCircumstances -> asRules(this)
        is Text -> emptyList()
    }

    // TODO revise if these should really be the semantics but it looks alright
    private fun asRules(r: Rule): List<ConfisRule> = listOf(
        ConfisRule(
            case = { r.sentence in q.sentence },
            then = { result = r.allowance.asResult }
        )
    )

    private fun asRules(c: SentenceWithCircumstances): List<ConfisRule> = when (c.rule.allowance) {
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
                )
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
}
