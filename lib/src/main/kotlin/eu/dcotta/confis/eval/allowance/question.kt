package eu.dcotta.confis.eval.allowance

import eu.dcotta.confis.eval.AllowanceQuestion
import eu.dcotta.confis.eval.askEngine
import eu.dcotta.confis.model.Agreement
import eu.dcotta.confis.model.AllowanceResult
import eu.dcotta.confis.model.AllowanceResult.Unspecified
import eu.dcotta.confis.util.with
import org.jeasy.rules.api.Facts

class AllowanceContext(facts: Facts, q2: AllowanceQuestion, defaultResult: AllowanceResult) {
    var result: AllowanceResult by facts with defaultResult
    val q: AllowanceQuestion by facts with q2
}

fun Agreement.ask(q: AllowanceQuestion, defaultResult: AllowanceResult = Unspecified): AllowanceResult = askEngine(
    clauseToRule = { asAllowanceRules(it) },
    buildContext = { AllowanceContext(it, q, defaultResult) },
    buildResult = { it.result }
)
