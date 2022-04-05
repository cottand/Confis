package eu.dcotta.confis.model

/**
 * A [Circumstance] that represents a [Sentence] that may have happened in the past
 * with respect to its corresponding clause
 */
data class PrecedentSentence(val sentence: Sentence) : Circumstance {

    override val key get() = Key

    override fun generalises(other: Circumstance) = other is PrecedentSentence && sentence generalises other.sentence

    override fun toString() = "PrecedentSentence($sentence)"

    companion object Key : Circumstance.Key<PrecedentSentence>
}
