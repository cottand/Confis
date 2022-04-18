package eu.dcotta.confis.model

import eu.dcotta.confis.model.Circumstance.Key
import kotlinx.collections.immutable.PersistentMap
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.plus
import kotlinx.collections.immutable.toPersistentMap

/**
 * A [CircumstanceMap] functions a lot like a CoroutineContext: it is a collection of [Circumstance]s that can be
 * narrowed down to their original type when extracting them, making this collection polymorphic.
 *
 * Two [Circumstance]s of the same type cannot coexist inside the same [CircumstanceMap]. This is ensured by the
 * uniqueness of their [Circumstance.Key], which is what uniquely identifies a [Circumstance] within a [CircumstanceMap]
 * (and what keeps track of its type).
 */
class CircumstanceMap private constructor(
    private val map: PersistentMap<Key<*>, Circumstance>,
) {

    override fun hashCode(): Int = 31 * map.hashCode()
    override fun equals(other: Any?): Boolean = other is CircumstanceMap && other.map == map

    @Suppress("UNCHECKED_CAST")
    operator fun <C : Circumstance> get(key: Key<C>): C? = map[key] as C?

    operator fun <C : Circumstance> get(key: Circumstance.SetKey<C>): List<C> {
        val set = map.keys.mapNotNull { with(key) { it.fromSetOrNull() } }
        return set.mapNotNull { get(it) }
    }

    operator fun plus(value: Circumstance): CircumstanceMap =
        CircumstanceMap(map + (value.key to value))

    operator fun plus(other: CircumstanceMap) = CircumstanceMap(map + other.map)

    fun generalises(key: Key<*>) = key in map

    infix fun generalises(circumstance: Circumstance): Boolean =
        get(circumstance.key)?.generalises(circumstance) ?: true

    /**
     * This [CircumstanceMap] generalises [otherCircumstances] when for every [Circumstance] `c` in this,
     * `c` generalises the [Circumstance] of the same type inside [otherCircumstances].
     */
    @Suppress("UNCHECKED_CAST")
    infix fun generalises(otherCircumstances: CircumstanceMap): Boolean {

        val otherMap = otherCircumstances.map

        return otherMap.keys.containsAll(map.keys) &&
            map.entries.all { (thisKey, thisCircumstance) ->
                thisCircumstance generalises (otherMap[thisKey] ?: error("Concurrent access error"))
            }
    }

    override fun toString(): String = "CircumstanceMap{${map.values.joinToString()}}"

    fun isEmpty(): Boolean = map.isEmpty()

    infix fun overlapsWith(other: CircumstanceMap): Boolean {
        val otherMap = other.map

        return otherMap.isEmpty() || isEmpty() ||
            map.entries.any { (thisKey, thisCircumstance) ->
                val otherCircumstance = otherMap[thisKey] ?: return@any false

                !(thisCircumstance disjoint otherCircumstance)
            }
    }

    infix fun disjoint(other: CircumstanceMap) = !overlapsWith(other)

    companion object {
        val empty = CircumstanceMap(persistentMapOf())
        fun of(vararg elems: Circumstance) = CircumstanceMap(elems.associateBy { it.key }.toPersistentMap())
    }
}
