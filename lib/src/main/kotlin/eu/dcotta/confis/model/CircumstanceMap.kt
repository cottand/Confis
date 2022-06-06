package eu.dcotta.confis.model

import eu.dcotta.confis.model.circumstance.Circumstance
import eu.dcotta.confis.model.circumstance.Circumstance.Key
import eu.dcotta.confis.model.circumstance.disjoint
import kotlinx.collections.immutable.PersistentMap
import kotlinx.collections.immutable.minus
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.plus
import kotlinx.collections.immutable.toPersistentList
import kotlinx.collections.immutable.toPersistentMap
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

/**
 * A [CircumstanceMap] functions a lot like a CoroutineContext: it is a collection of [Circumstance]s that can be
 * narrowed down to their original type when extracting them, making this collection polymorphic.
 *
 * Two [Circumstance]s of the same type cannot coexist inside the same [CircumstanceMap]. This is ensured by the
 * uniqueness of their [Circumstance.Key], which is what uniquely identifies a [Circumstance] within a [CircumstanceMap]
 * (and what keeps track of its type).
 */
@Serializable(with = CircumstanceMap.Serializer::class)
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

    infix fun <T : Circumstance> except(keySet: Circumstance.SetKey<T>): CircumstanceMap {
        val set = get(keySet)
        return CircumstanceMap(map.minus(set.map { it.key }))
    }

    override fun toString(): String = "CircumstanceMap{${map.values.joinToString()}}"

    fun render(): String = map.values.joinToString(transform = Circumstance::render)

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

    fun toList(): List<Circumstance> = map.values.toPersistentList()

    companion object {
        val empty = CircumstanceMap(persistentMapOf())
        fun of(vararg elems: Circumstance) = CircumstanceMap(elems.associateBy { it.key }.toPersistentMap())
    }

    object Serializer : KSerializer<CircumstanceMap> {

        private val mapSerializer: KSerializer<Map<Key<Circumstance>, Circumstance>> =
            MapSerializer(Key.serializer(Circumstance.serializer()), Circumstance.serializer())

        @Suppress("OPT_IN_IS_NOT_ENABLED")
        @OptIn(ExperimentalSerializationApi::class)
        override val descriptor: SerialDescriptor =
            SerialDescriptor("CircumstanceMap", mapSerializer.descriptor)

        override fun deserialize(decoder: Decoder): CircumstanceMap {
            val decoded = decoder.decodeSerializableValue(mapSerializer)
            return CircumstanceMap(decoded.toPersistentMap())
        }

        override fun serialize(encoder: Encoder, value: CircumstanceMap) {
            encoder.encodeSerializableValue(mapSerializer, value.map)
        }
    }
}
