package eu.dcotta.confis.dsl

import eu.dcotta.confis.model.Allowance
import eu.dcotta.confis.model.CircumstanceMap
import eu.dcotta.confis.model.Clause.Rule
import eu.dcotta.confis.model.Clause.SentenceWithCircumstances
import eu.dcotta.confis.model.Purpose
import eu.dcotta.confis.model.PurposePolicy
import eu.dcotta.confis.model.TimeRange

@DslMarker
annotation class CircumstanceDsl

@ConfisDsl
class CircumstanceBuilder(private val rule: Rule, private val circumstanceAllowance: Allowance) {
    private val purposePolicies = mutableListOf<Purpose>()
    private var circumstances = CircumstanceMap.of(PurposePolicy(purposePolicies))

    // purposes
    object PurposeReceiver

    val with = PurposeReceiver

    infix fun PurposeReceiver.purpose(purpose: Purpose) {
        purposePolicies += purpose
    }

    // time
    fun within(init: () -> TimeRange.Range) {
        circumstances += init()
    }

    internal fun build(): SentenceWithCircumstances {
        circumstances += PurposePolicy(purposePolicies)
        return SentenceWithCircumstances(rule, circumstanceAllowance, circumstances)
    }
}
