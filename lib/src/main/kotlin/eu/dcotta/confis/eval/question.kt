package eu.dcotta.confis.eval

import com.deliveredtechnologies.rulebook.Fact
import com.deliveredtechnologies.rulebook.FactMap
import com.deliveredtechnologies.rulebook.Result
import com.deliveredtechnologies.rulebook.lang.RuleBookBuilder
import com.deliveredtechnologies.rulebook.lang.RuleBuilder
import eu.dcotta.confis.dsl.AgreementBuilder
import eu.dcotta.confis.model.Agreement
import eu.dcotta.confis.model.Allowance
import eu.dcotta.confis.model.AllowanceResult
import eu.dcotta.confis.model.Clause
import eu.dcotta.confis.model.Clause.EncodedSentence
import eu.dcotta.confis.model.Clause.Text
import eu.dcotta.confis.model.Obj.Anything
import eu.dcotta.confis.model.Purpose
import eu.dcotta.confis.model.Sentence

data class AllowanceQuestion(
    val sentence: Sentence,
    val purpose: Purpose? = null,
)

data class FactAndQuestion(val q: AllowanceQuestion)

class QueryableAgreement(val agreement: Agreement) {

    constructor(init: AgreementBuilder.() -> Unit) : this(AgreementBuilder(init))

    private val rb = RuleBookBuilder.create()
        .withResultType(AllowanceResult::class.java)
        .withDefaultResult(AllowanceResult.Unspecified)
        .build()

    init {
        for (clause in agreement.clauses.flatMap { it.asRules() })
            rb.addRule(
                RuleBuilder.create()
                    .withFactType(FactAndQuestion::class.java)
                    .withResultType(AllowanceResult::class.java)
                    .`when` { clause.case(it.one) }
                    .then { facts, result -> clause.then(ThenBuilder(result, facts.one)) }
                    .build()
            )
    }

    fun ask(q: AllowanceQuestion): AllowanceResult {
        rb.run(FactMap(Fact(FactAndQuestion(q))))
        return rb.result.orElse(null)?.value ?: error("Should contain a result")
    }
}

class ThenBuilder(private val allowanceResult: Result<AllowanceResult>, var factAndQuestion: FactAndQuestion) {
    var result: AllowanceResult
        get() = allowanceResult.value
        set(value) {
            allowanceResult.value = value
        }
}

data class ConfisRule(val case: FactAndQuestion.() -> Boolean, val then: ThenBuilder.() -> Unit)

fun Clause.asRules(): List<ConfisRule> = when (this) {
    is EncodedSentence -> canonifyClause(this).flatMap { (a, at) -> at.asRule(a) }
    is Text -> emptyList()
}

fun Atom.asRule(allowance: Allowance): List<ConfisRule> = when {
    purpose == null && sentence.obj == Anything -> listOf(ConfisRule(
        case = { sentence.subject == q.sentence.subject && sentence.action == q.sentence.action },
        then = { result = allowance.asResult },
    ))
    purpose == null -> listOf(
        ConfisRule(
            // neither clause nor question specify purpose
            case = { sentence == q.sentence && q.purpose == null },
            then = { result = allowance.asResult },
        ),
        ConfisRule(
            case = { sentence == q.sentence },
            then = { result = allowance.asResult },
        ),
    )
    else -> listOf(ConfisRule(
        case = { sentence == q.sentence && purpose == q.purpose },
        then = { result = allowance.asResult },
    ))
}
