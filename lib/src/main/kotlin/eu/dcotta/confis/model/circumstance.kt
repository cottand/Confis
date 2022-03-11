package eu.dcotta.confis.model

interface Circumstance {

    /**
     * whether given [other] circumstances, are we in this [Circumstance].
     *
     * Ie, does this [Circumstance] generalise the [other] circumstance
     */
    operator fun contains(other: Circumstance): Boolean

    interface Key<T : Circumstance>

    val key: Key<*>
}

operator fun Circumstance.plus(other: Circumstance) = CircumstanceMap.of(this, other)
