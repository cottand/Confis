package eu.dcotta.confis.dsl

import eu.dcotta.confis.model.Action
import eu.dcotta.confis.model.Obj
import eu.dcotta.confis.model.Obj.Anything
import eu.dcotta.confis.model.Sentence
import eu.dcotta.confis.model.Subject

@DslMarker
annotation class ConfisDsl

@ConfisDsl
class SentenceBuilder(private val subject: Subject) {
    infix operator fun Action.invoke(obj: Obj) = Sentence(subject, this, obj)
    operator fun Action.invoke() = Sentence(subject, this, Anything)
}
