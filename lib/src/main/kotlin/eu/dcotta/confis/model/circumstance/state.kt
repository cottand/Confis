package eu.dcotta.confis.model.circumstance

import eu.dcotta.confis.model.CircumstanceMap
import eu.dcotta.confis.model.Sentence
import kotlinx.collections.immutable.PersistentMap

typealias WorldState = PersistentMap<Sentence, CircumstanceMap>

data class PastAction(val sentence: Sentence, val circumstances: CircumstanceMap) {
    infix fun happenedIn(world: WorldState) = world[sentence]?.let { pastCircumstances ->
        circumstances generalises pastCircumstances
    } ?: false

    infix fun possiblyHappenedIn(world: WorldState) = world[sentence]?.let { pastCircumstances ->
        circumstances overlapsWith pastCircumstances
    } ?: false
}

fun WorldState.render(): String = toList().joinToString { (sentence, cs) ->
    buildString {
        append("  - '")
        append(sentence.subject)
        append(' ')
        append(sentence.action)
        append(' ')
        append(sentence.obj)
        append(".' under the following circumstances:")
        append(cs.toList().joinToString { "\n    - ${it.render()}" })
    }
}
