package eu.dcotta.confis.eval

import eu.dcotta.confis.model.Agreement
import eu.dcotta.confis.model.Clause
import org.jeasy.rules.api.Facts
import org.jeasy.rules.api.Rules
import org.jeasy.rules.core.AbstractRulesEngine
import org.jeasy.rules.core.DefaultRulesEngine
import org.jeasy.rules.core.RuleBuilder

interface ConfisRule<Ctx> {
    val case: Ctx.() -> Boolean
    val then: Ctx.() -> Unit
}

fun <Ctx, R : ConfisRule<Ctx>, E> Agreement.askEngine(
    clauseToRule: (clause: Clause) -> Iterable<R>,
    buildContext: (Facts) -> Ctx,
    // lower number, higher priority
    priority: (ruleIndex: Int) -> Int = { -it },
    rulesEngine: AbstractRulesEngine = DefaultRulesEngine(),
    buildResult: (Ctx) -> E
): E {
    val rs = clauses.flatMap { c -> clauseToRule(c).map { r -> c to r } }
        .mapIndexed { index, (clause, confisRule) ->
            RuleBuilder()
                .name("${clause::class.simpleName}#$index")
                .description(clause.toString())
                // rules have ordering as written in the contract - later -> higher priority (low number)
                .priority(priority(index))
                .`when` { fs -> confisRule.case(buildContext(fs)) }
                .then { fs -> confisRule.then(buildContext(fs)) }
                .build()
        }
        .toSet()
        .let(::Rules)

    val facts = Facts()

    rulesEngine.fire(rs, facts)

    return buildResult(buildContext(facts))
}

interface QueryResponse {
    fun render(): String
}
