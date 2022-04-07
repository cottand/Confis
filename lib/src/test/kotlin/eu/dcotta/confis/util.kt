package eu.dcotta.confis

import eu.dcotta.confis.dsl.AgreementBuilder
import io.kotest.matchers.should
import io.kotest.matchers.types.beOfType

fun agreementExtract(init: AgreementBuilder.() -> Unit) = init

inline fun <reified T : Any> Any.asOrFail(): T {
    this should beOfType<T>()
    return this as T
}
