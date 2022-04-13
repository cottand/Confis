package eu.dcotta.confis.model

interface Circumstance {

    /**
     * whether given [other] circumstances, are we in this [Circumstance].
     *
     * Ie, does this [Circumstance] generalise the [other] circumstance
     */
    infix fun generalises(other: Circumstance): Boolean

    interface Key<T : Circumstance>

    val key: Key<*>
}

infix fun Circumstance.disjoint(other: Circumstance) = when (this) {
    is OverlappingCircumstance -> !this.overlapsWith(other)
    else -> !other.generalises(this) && !this.generalises(other)
}

interface OverlappingCircumstance : Circumstance {
    infix fun overlapsWith(other: Circumstance): Boolean
}

operator fun Circumstance.plus(other: Circumstance) = CircumstanceMap.of(this, other)
