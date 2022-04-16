package eu.dcotta.confis.util

import eu.dcotta.confis.model.Circumstance
import eu.dcotta.confis.model.CircumstanceMap
import kotlinx.collections.immutable.PersistentSet
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

fun <T> MutableList<T>.removeLastOccurrence(item: T) = removeAt(lastIndexOf(item))

fun <T, S> PersistentSet<T>.mapPersistent(transform: (T) -> S): PersistentSet<S> =
    mapTo(persistentSetOf<S>().builder(), transform).build()

operator fun PersistentSet<CircumstanceMap>.plus(c: Circumstance): PersistentSet<CircumstanceMap> =
    if (isEmpty()) persistentSetOf(CircumstanceMap.of(c))
    else mapPersistent { it + c }
