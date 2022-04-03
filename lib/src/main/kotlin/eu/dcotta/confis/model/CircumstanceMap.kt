package eu.dcotta.confis.model

import eu.dcotta.confis.model.Circumstance.Key
import kotlinx.collections.immutable.PersistentMap
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.plus
import kotlinx.collections.immutable.toPersistentMap

class CircumstanceMap private constructor(
    private val map: PersistentMap<Key<*>, Circumstance>,
) {

    override fun hashCode(): Int = 31 * map.hashCode()
    override fun equals(other: Any?): Boolean = other is CircumstanceMap && other.map == map

    @Suppress("UNCHECKED_CAST")
    operator fun <C : Circumstance> get(key: Circumstance.Key<C>): C? = map[key] as C?

    operator fun plus(value: Circumstance): CircumstanceMap =
        CircumstanceMap(map + (value.key to value))

    operator fun plus(other: CircumstanceMap) = CircumstanceMap(map + other.map)

    fun generalises(key: Key<*>) = key in map

    @Suppress("UNCHECKED_CAST")
    infix fun generalises(otherCircumstances: CircumstanceMap): Boolean {

        val otherMap = otherCircumstances.map

        return otherMap.keys.containsAll(map.keys) &&
            map.entries.all { (thisKey, thisCircumstance) ->
                thisCircumstance generalises (otherMap[thisKey] ?: error("Concurrent access error"))
            }
    }

    infix fun disjoint(other: CircumstanceMap) = !other.generalises(this) && !this.generalises(other)

    override fun toString(): String = "CircumstanceMap{${map.values.joinToString()}}"

    companion object {
        val empty = CircumstanceMap(persistentMapOf())
        fun of(vararg elems: Circumstance) = CircumstanceMap(elems.associateBy { it.key }.toPersistentMap())
    }
}
