package eu.dcotta.confis.dsl

import eu.dcotta.confis.model.CircumstanceMap
import eu.dcotta.confis.model.Party
import eu.dcotta.confis.model.Sentence
import eu.dcotta.confis.model.Subject
import eu.dcotta.confis.model.circumstance.Consent
import eu.dcotta.confis.model.circumstance.Date
import eu.dcotta.confis.model.circumstance.PrecedentSentence
import eu.dcotta.confis.model.circumstance.Purpose
import eu.dcotta.confis.model.circumstance.PurposePolicy
import eu.dcotta.confis.model.circumstance.TimeRange

annotation class CircumstanceDsl

@ConfisDsl
class CircumstanceBuilder(val sentence: Sentence) {
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

    @ConfisDsl
    infix fun PurposeReceiver.consentFrom(party: Party) {
        circumstances += Consent(sentence, party)
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

    fun after(date: Date) {
        circumstances += TimeRange.OpenFutureRange(date)
    }

    fun at(date: () -> Date) {
        circumstances += date()
    }

    // conditional

    object PastSentenceBuilder {
        infix fun Subject.did(actionObject: ActionObject) = Sentence(this, actionObject.action, actionObject.obj)
    }

    /**
     * Starts a precedent circumstance, where the [Subject] must have actioned [sentence]
     */
    fun after(sentence: PastSentenceBuilder.() -> Sentence) {
        circumstances += PrecedentSentence(sentence(PastSentenceBuilder))
    }

    fun `$$build$$`(): CircumstanceMap {
        if (purposePolicies.isNotEmpty()) circumstances += PurposePolicy(purposePolicies)
        return circumstances
    }
}

fun circumstanceContainer(init: CircumstanceBuilder.() -> Unit) = init
