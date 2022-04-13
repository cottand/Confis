package eu.dcotta.confis.dsl

import eu.dcotta.confis.model.Allowance
import eu.dcotta.confis.model.CircumstanceMap
import eu.dcotta.confis.model.Clause.Rule
import eu.dcotta.confis.model.Clause.SentenceWithCircumstances
import eu.dcotta.confis.model.PrecedentSentence
import eu.dcotta.confis.model.Purpose
import eu.dcotta.confis.model.PurposePolicy
import eu.dcotta.confis.model.Sentence
import eu.dcotta.confis.model.Subject
import eu.dcotta.confis.model.TimeRange

@DslMarker
annotation class CircumstanceDsl

@ConfisDsl
class CircumstanceBuilder(private val rule: Rule, private val circumstanceAllowance: Allowance) {
    private val purposePolicies = mutableListOf<Purpose>()
    private var circumstances = CircumstanceMap.empty

    // purposes
    object PurposeReceiver

    /**
     * Starts a [Purpose] circumstance. For example:
     *
     * ```kotlin
     * with purpose Research
     * ```
     */
    @ConfisDsl
    val with = PurposeReceiver

    @ConfisDsl
    infix fun PurposeReceiver.purpose(purpose: Purpose) {
        purposePolicies += purpose
    }

    // time

    /**
     * Specifies a [TimeRange] circumstance. For example:
     *
     *
     * ```kotlin
     * within { (1 of January year 2020)..(16 of May year 2025) }
     * ```
     */
    fun within(timePeriod: () -> TimeRange.Range) {
        circumstances += timePeriod()
    }

    // conditional

    /**
     * Starts a precedent circumstance, where the [Subject] must have actioned [sentence]
     */
    infix fun Subject.did(sentence: SentenceBuilder.() -> Sentence) {
        val s = SentenceBuilder(this).sentence()
        circumstances += PrecedentSentence(s)
    }

    internal fun build(): SentenceWithCircumstances {
        if (purposePolicies.isNotEmpty()) circumstances += PurposePolicy(purposePolicies)
        return SentenceWithCircumstances(rule, circumstanceAllowance, circumstances)
    }
}
