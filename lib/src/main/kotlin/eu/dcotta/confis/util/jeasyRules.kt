package eu.dcotta.confis.util

import org.jeasy.rules.api.Facts
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

infix fun <T> Facts.with(default: T) = object : ReadWriteProperty<Any, T> {

    override fun getValue(thisRef: Any, property: KProperty<*>): T = get(property.name) ?: default

    override fun setValue(thisRef: Any, property: KProperty<*>, value: T) = put(property.name, value)
}
