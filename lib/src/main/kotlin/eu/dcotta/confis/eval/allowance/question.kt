package eu.dcotta.confis.eval.allowance

import eu.dcotta.confis.eval.askEngine
import eu.dcotta.confis.model.Agreement
import eu.dcotta.confis.model.AllowanceResult
import eu.dcotta.confis.model.AllowanceResult.Unspecified
import eu.dcotta.confis.model.CircumstanceMap
import eu.dcotta.confis.model.Purpose
import eu.dcotta.confis.model.PurposePolicy
import eu.dcotta.confis.model.Sentence
import eu.dcotta.confis.util.with
import org.jeasy.rules.api.Facts

data class AllowanceQuestion(
    val sentence: Sentence,
    val circumstances: CircumstanceMap = CircumstanceMap.empty,
) {
    constructor(sentence: Sentence, purpose: Purpose) :
        this(sentence, CircumstanceMap.of(PurposePolicy(purpose)))
}

class AllowanceContext(facts: Facts, q2: AllowanceQuestion, defaultResult: AllowanceResult) {
    var result: AllowanceResult by facts with defaultResult
    val q: AllowanceQuestion by facts with q2
}

fun Agreement.ask(q: AllowanceQuestion, defaultResult: AllowanceResult = Unspecified): AllowanceResult = askEngine(
    clauseToRule = { asAllowanceRules(it) },
    buildContext = { AllowanceContext(it, q, defaultResult) },
    buildResult = { it.result }
)
