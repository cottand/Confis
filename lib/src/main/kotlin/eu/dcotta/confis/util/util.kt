package eu.dcotta.confis.util

import eu.dcotta.confis.model.CircumstanceMap
import eu.dcotta.confis.model.circumstance.Circumstance
import kotlinx.collections.immutable.PersistentSet
import kotlinx.collections.immutable.persistentHashMapOf
import kotlinx.collections.immutable.persistentSetOf
import kotlin.properties.PropertyDelegateProvider
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

fun <C, V> oneTimeProperty(instantiate: (prop: KProperty<*>) -> V) =
    PropertyDelegateProvider<C, ReadOnlyProperty<C, V>> { _, prop ->
        val v = instantiate(prop)
        ReadOnlyProperty { _, _ -> v }
    }

fun <K, V> Map<K, V?>.filterValuesNotNull(): Map<K, V> =
    mapNotNull { (k, v) -> if (v == null) null else (k to v) }
        .toMap()

fun <K, V, W> Map<K, V>.mapValuesNotNull(transform: (Map.Entry<K, V>) -> W?): Map<K, W> {
    val dest = persistentHashMapOf<K, W>().builder()
    forEach { entry -> transform(entry)?.let { dest += entry.key to it } }
    return dest.build()
}

fun <T> MutableList<T>.removeLastOccurrence(item: T) = removeAt(lastIndexOf(item))

fun <T, S> PersistentSet<T>.mapPersistent(transform: (T) -> S): PersistentSet<S> =
    mapTo(persistentSetOf<S>().builder(), transform).build()

operator fun PersistentSet<CircumstanceMap>.plus(c: Circumstance): PersistentSet<CircumstanceMap> =
    if (isEmpty()) persistentSetOf(CircumstanceMap.of(c))
    else mapPersistent { it + c }
