package eu.dcotta.confis.model.circumstance

import eu.dcotta.confis.model.Party
import eu.dcotta.confis.model.Sentence
import eu.dcotta.confis.model.circumstance.Circumstance.Key
import eu.dcotta.confis.model.circumstance.Circumstance.SetKey

data class Consent(val sentence: Sentence, val from: Party) : Circumstance {

    override fun generalises(other: Circumstance): Boolean = other is Consent &&
        sentence generalises other.sentence &&
        from == other.from

    override val key: Key<*>
        get() = PartyKey(from)

    @JvmInline
    value class PartyKey(val party: Party) : Key<Consent>

    override fun render(): String =
        "with the consent from ${from.render()}"

    companion object KeySet : SetKey<Consent> {
        override fun Key<*>.fromSetOrNull() = this as? PartyKey
    }
}
