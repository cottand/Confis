package eu.dcotta.confis

import eu.dcotta.confis.dsl.AgreementBuilder
import eu.dcotta.confis.model.Action
import eu.dcotta.confis.model.Obj
import eu.dcotta.confis.model.Party
import eu.dcotta.confis.model.Sentence
import io.kotest.matchers.should
import io.kotest.matchers.types.beOfType

fun agreementExtract(init: AgreementBuilder.() -> Unit) = init

inline fun <reified T : Any> Any.asOrFail(): T {
    this should beOfType<T>()
    return this as T
}

fun Sentence(builder: SentenceTestBuilder.() -> Sentence): Sentence =
    SentenceTestBuilder.builder()

object SentenceTestBuilder {
    operator fun String.invoke(action: String, obj: Obj) = Sentence(Party(this), Action(action), obj)
}
