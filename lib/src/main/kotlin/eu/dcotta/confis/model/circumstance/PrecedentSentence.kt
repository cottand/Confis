package eu.dcotta.confis.model.circumstance

import eu.dcotta.confis.model.Sentence
import eu.dcotta.confis.model.circumstance.Circumstance.SetKey
import kotlinx.serialization.Serializable

/**
 * A [Circumstance] that represents a [Sentence] that may have happened in the past
 * with respect to its corresponding clause
 */
@Serializable
data class PrecedentSentence(val sentence: Sentence) : Circumstance {

    override val key get() = Key(sentence)

    override fun generalises(other: Circumstance) = other is PrecedentSentence && sentence generalises other.sentence

    override fun toString() = "PrecedentSentence($sentence)"

    override fun render() = with(sentence) {
        "only after ${subject.render()} did ${action.render()} ${obj.render()}"
    }

    @JvmInline
    @Serializable
    value class Key(val sentence: Sentence) : Circumstance.Key<PrecedentSentence>

    companion object KeySet : SetKey<PrecedentSentence> {
        override fun Circumstance.Key<*>.fromSetOrNull() = this as? Key
    }
}
