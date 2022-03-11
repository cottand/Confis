package eu.dcotta.confis.model

import eu.dcotta.confis.model.Circumstance.Key
import kotlinx.collections.immutable.PersistentMap
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.plus
import kotlinx.collections.immutable.toPersistentMap

@JvmInline
value class CircumstanceMap private constructor(
    private val initial: PersistentMap<Key<*>, Circumstance>,
) {

    @Suppress("UNCHECKED_CAST")
    operator fun <C : Circumstance> get(key: Circumstance.Key<C>): C? = initial[key] as C?

    operator fun <T : Any> plus(value: Circumstance): CircumstanceMap =
        CircumstanceMap(initial + (value.key to value))

    operator fun plus(other: CircumstanceMap) = CircumstanceMap(initial + other.initial)

    operator fun contains(key: Circumstance.Key<*>) = key in initial

    @Suppress("UNCHECKED_CAST")
    operator fun contains(otherCircumstances: CircumstanceMap): Boolean =
        initial.entries.containsAll(otherCircumstances.initial.entries) &&
            otherCircumstances.initial.entries.all { (k: Circumstance.Key<*>, otherCircumstance: Circumstance) ->
                val thisCircumstance = initial[k] ?: error("Concurrent access error")
                otherCircumstance.contains(thisCircumstance)
            }

    infix fun disjoint(other: CircumstanceMap) = this !in other && other !in this

    override fun toString(): String = "CircumstanceMap{${initial.values.joinToString()}}"

    companion object {
        val empty = CircumstanceMap(persistentMapOf())
        fun of(vararg elems: Circumstance) = CircumstanceMap(elems.associateBy { it.key }.toPersistentMap())
    }
}
