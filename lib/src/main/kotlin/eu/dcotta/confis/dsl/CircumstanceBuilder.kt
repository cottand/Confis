package eu.dcotta.confis.dsl

import eu.dcotta.confis.model.Allowance
import eu.dcotta.confis.model.CircumstanceMap
import eu.dcotta.confis.model.Clause.Rule
import eu.dcotta.confis.model.Clause.SentenceWithCircumstances
import eu.dcotta.confis.model.Purpose
import eu.dcotta.confis.model.PurposePolicy
import eu.dcotta.confis.model.TimeRange

@ConfisDsl
class CircumstanceBuilder(private val rule: Rule, private val circumstanceAllowance: Allowance) {
    private val purposePolicies = mutableListOf<Purpose>()
    private var time: TimeRange? = null

    // purposes
    object PurposeReceiver

    val with = PurposeReceiver

    infix fun PurposeReceiver.purpose(purpose: Purpose) {
        purposePolicies += purpose
    }

    // time
    fun within(init: () -> TimeRange.Range) {
        time = init()
    }

    internal fun build(): SentenceWithCircumstances =
        SentenceWithCircumstances(rule, circumstanceAllowance, CircumstanceMap.of(PurposePolicy(purposePolicies)))
}
