package eu.dcotta.confis.model

import eu.dcotta.confis.model.Circumstance.Key

enum class Purpose {
    Commercial, Research;
}

data class PurposePolicy(val purposes: List<Purpose>) : Circumstance {
    constructor(vararg purposes: Purpose) : this(purposes.asList())

    override val key get() = Companion

    companion object : Key<PurposePolicy>

    override fun generalises(other: Circumstance) = other is PurposePolicy && other.purposes.containsAll(purposes)

    override fun toString(): String = "Policies$purposes"
}
