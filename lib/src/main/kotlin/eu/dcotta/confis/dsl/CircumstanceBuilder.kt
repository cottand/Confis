package eu.dcotta.confis.dsl

import eu.dcotta.confis.model.Allowance
import eu.dcotta.confis.model.CircumstanceMap
import eu.dcotta.confis.model.Clause.Rule
import eu.dcotta.confis.model.Clause.SentenceWithCircumstances
import eu.dcotta.confis.model.Purpose
import eu.dcotta.confis.model.PurposePolicy

@ConfisDsl
class CircumstanceBuilder(private val rule: Rule, private val circumstanceAllowance: Allowance) {
    private val purposePolicies = mutableListOf<Purpose>()
    data class PurposeNarrower(val purposes: List<Purpose>)

    fun include(vararg purposes: Purpose) = PurposeNarrower(purposes.asList())

    object PurposeReceiver

    infix fun PurposeReceiver.purpose(purpose: Purpose) {
        purposePolicies += purpose
    }

    val with = PurposeReceiver

    internal fun build(): SentenceWithCircumstances =
        SentenceWithCircumstances(rule, circumstanceAllowance, CircumstanceMap.of(PurposePolicy(purposePolicies)))
}
