package eu.dcotta.confis.model.circumstance

import eu.dcotta.confis.model.circumstance.Circumstance.Key

enum class Purpose {
    Commercial, Research, Internal;
}

data class PurposePolicy(val purposes: List<Purpose>) : Circumstance {
    constructor(vararg purposes: Purpose) : this(purposes.asList())

    override val key get() = Companion

    companion object : Key<PurposePolicy>

    override fun generalises(other: Circumstance) = other is PurposePolicy && other.purposes.containsAll(purposes)

    override fun toString(): String = "Policies$purposes"

    override fun render() =
        if (purposes.size == 1) "with ${purposes.first()} purpose"
        else "with ${purposes.joinToString()} purposes"
}
