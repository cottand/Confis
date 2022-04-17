package eu.dcotta.confis.model

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
