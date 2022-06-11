package eu.dcotta.confis.util

import kotlinx.collections.immutable.persistentHashMapOf
import kotlin.properties.PropertyDelegateProvider
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

fun <C, V> oneTimeProperty(instantiate: (prop: KProperty<*>) -> V) =
    PropertyDelegateProvider<C, ReadOnlyProperty<C, V>> { _, prop ->
        val v = instantiate(prop)
        ReadOnlyProperty { _, _ -> v }
    }

fun <K, V, W> Map<K, V>.mapValuesNotNull(transform: (Map.Entry<K, V>) -> W?): Map<K, W> {
    val dest = persistentHashMapOf<K, W>().builder()
    forEach { entry -> transform(entry)?.let { dest += entry.key to it } }
    return dest.build()
}

fun <T> MutableList<T>.removeLastOccurrence(item: T) = removeAt(lastIndexOf(item))
