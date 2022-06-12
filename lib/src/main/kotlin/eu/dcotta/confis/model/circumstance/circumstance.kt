package eu.dcotta.confis.model.circumstance

import eu.dcotta.confis.model.CircumstanceMap
import kotlinx.serialization.Serializable

@Serializable
sealed interface Circumstance {

    /**
     * whether given [other] circumstances, are we in this [Circumstance].
     *
     * Ie, does this [Circumstance] generalise the [other] circumstance
     */
    infix fun generalises(other: Circumstance): Boolean

    @Serializable
    sealed interface Key<out T : Circumstance>

    interface SetKey<T : Circumstance> {
        fun Key<*>.fromSetOrNull(): Key<T>?
    }

    @Serializable
    val key: Key<*>

    fun render(): String = toString()
}

infix fun Circumstance.disjoint(other: Circumstance) = when (this) {
    is OverlappingCircumstance -> !this.overlapsWith(other)
    else -> !other.generalises(this) && !this.generalises(other)
}

@Serializable
sealed interface OverlappingCircumstance : Circumstance {
    infix fun overlapsWith(other: Circumstance): Boolean
}

operator fun Circumstance.plus(other: Circumstance) = CircumstanceMap.of(this, other)

interface TestCircumstance : Circumstance {
    interface Key<T : TestCircumstance> : Circumstance.Key<T>
}
