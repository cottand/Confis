package eu.dcotta.confis.dsl

import eu.dcotta.confis.model.Action
import eu.dcotta.confis.model.Obj
import eu.dcotta.confis.model.Obj.Anything
import eu.dcotta.confis.model.Sentence
import eu.dcotta.confis.model.Subject

@DslMarker
annotation class ConfisDsl

// @DslMarker
// annotation class SentenceDsl

@ConfisDsl
class SentenceBuilder(private val subject: Subject) {
    // @SentenceDsl
    infix operator fun Action.invoke(obj: Obj) = Sentence(subject, this, obj)
    // @SentenceDsl
    operator fun Action.invoke() = Sentence(subject, this, Anything)
}
