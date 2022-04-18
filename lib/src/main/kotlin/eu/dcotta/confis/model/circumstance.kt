package eu.dcotta.confis.model

interface Circumstance {

    /**
     * whether given [other] circumstances, are we in this [Circumstance].
     *
     * Ie, does this [Circumstance] generalise the [other] circumstance
     */
    infix fun generalises(other: Circumstance): Boolean

    interface Key<T : Circumstance>

    interface SetKey<T : Circumstance> {
        fun Key<*>.fromSetOrNull(): Key<T>?
    }

    val key: Key<*>

    fun render(): String = toString()
}

infix fun Circumstance.disjoint(other: Circumstance) = when (this) {
    is OverlappingCircumstance -> !this.overlapsWith(other)
    else -> !other.generalises(this) && !this.generalises(other)
}

/**
 * Similar to [Circumstance.generalises], and the map can be more specific for other [Circumstance]s.
 *
 * Equivalent to `CircumstanceMap.of(this) generalises map`
 */
infix fun Circumstance.generalises(map: CircumstanceMap) = map[key]?.let { generalises(it) } ?: false

interface OverlappingCircumstance : Circumstance {
    infix fun overlapsWith(other: Circumstance): Boolean
}

operator fun Circumstance.plus(other: Circumstance) = CircumstanceMap.of(this, other)
