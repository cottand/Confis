package eu.dcotta.confis.dsl

import eu.dcotta.confis.model.CircumstanceMap
import eu.dcotta.confis.model.PrecedentSentence
import eu.dcotta.confis.model.Purpose
import eu.dcotta.confis.model.PurposePolicy
import eu.dcotta.confis.model.Sentence
import eu.dcotta.confis.model.Subject
import eu.dcotta.confis.model.TimeRange

annotation class CircumstanceDsl

@ConfisDsl
class CircumstanceBuilder {
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

    object PastSentenceBuilder {
        infix fun Subject.did(actionObject: ActionObject) = Sentence(this, actionObject.action, actionObject.obj)
    }

    /**
     * Starts a precedent circumstance, where the [Subject] must have actioned [sentence]
     */
    fun after(sentence: PastSentenceBuilder.() -> Sentence) {
        circumstances += PrecedentSentence(sentence(PastSentenceBuilder))
    }

    internal fun build(): CircumstanceMap {
        if (purposePolicies.isNotEmpty()) circumstances += PurposePolicy(purposePolicies)
        return circumstances
    }
}
