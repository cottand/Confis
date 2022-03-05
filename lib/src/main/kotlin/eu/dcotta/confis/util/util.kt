package eu.dcotta.confis.util

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
