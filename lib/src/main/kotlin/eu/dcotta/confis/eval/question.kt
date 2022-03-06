package eu.dcotta.confis.eval

import com.deliveredtechnologies.rulebook.Result
import com.deliveredtechnologies.rulebook.lang.RuleBookBuilder
import com.deliveredtechnologies.rulebook.lang.RuleBuilder
import com.deliveredtechnologies.rulebook.model.rulechain.cor.CoRRuleBook
import eu.dcotta.confis.model.Action
import eu.dcotta.confis.model.Agreement
import eu.dcotta.confis.model.AllowanceResult
import eu.dcotta.confis.model.Clause
import eu.dcotta.confis.model.Clause.EncodedSentence
import eu.dcotta.confis.model.Clause.Text
import eu.dcotta.confis.model.Obj
import eu.dcotta.confis.model.Purpose
import eu.dcotta.confis.model.Subject
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

data class Question(
    val subject: Subject,
    val action: Action,
    val obj: Obj,
    val purpose: Purpose? = null,
)

data class FactAndQuestion(val q: Question)

class QueryableAgreement(val agreement: Agreement) {
    private val ruleBook = object : CoRRuleBook<AllowanceResult>() {
        override fun defineRules() {
            for (clause in agreement.clauses.map { it.asRule() })
                addRule(
                    RuleBuilder.create()
                        .withFactType(FactAndQuestion::class.java)
                        .withResultType(AllowanceResult::class.java)
                        .`when` {
                            clause.pred(it.one)
                        }
                        .then { facts, result -> ThenBuilder(result, facts.one) }
                        .build()
                )
        }
    }

    val rb = RuleBookBuilder.create(ruleBook::class.java)
        .withResultType(AllowanceResult::class.java)
        .withDefaultResult(AllowanceResult.Unspecified)
        .build()
}

class ThenBuilder(allowanceResult: Result<AllowanceResult>, var factAndQuestion: FactAndQuestion) {
    val result = object : ReadWriteProperty<ThenBuilder, AllowanceResult> {
        override fun setValue(thisRef: ThenBuilder, property: KProperty<*>, value: AllowanceResult) {
            allowanceResult.value = value
        }

        override fun getValue(thisRef: ThenBuilder, property: KProperty<*>): AllowanceResult = allowanceResult.value
    }
}

data class ConfisRule(val pred: FactAndQuestion.() -> Boolean, val then: ThenBuilder.() -> Unit) {
    companion object {
        val empty = ConfisRule({ false }) { }
    }
}

fun Clause.asRule(): ConfisRule = when (this) {
    is EncodedSentence -> TODO()
    is Text -> ConfisRule.empty
}
