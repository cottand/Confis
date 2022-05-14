package eu.dcotta.confis.eval

import eu.dcotta.confis.model.CircumstanceMap
import eu.dcotta.confis.model.CircumstanceMap.Companion
import eu.dcotta.confis.model.Sentence
import eu.dcotta.confis.model.circumstance.Purpose
import eu.dcotta.confis.model.circumstance.PurposePolicy
import eu.dcotta.confis.model.circumstance.WorldState
import kotlinx.collections.immutable.persistentHashMapOf
import kotlinx.collections.immutable.toPersistentMap

sealed interface ConfisQuery

/**
 * Question meant to represent _'What do I need to do in order to comply to the agreement?'_
 */
@JvmInline
value class ComplianceQuestion(val state: WorldState) : ConfisQuery {
    constructor() : this(persistentHashMapOf())

    constructor(vararg sentences: Sentence) :
        this(persistentHashMapOf(*sentences.map { it to CircumstanceMap.empty }.toTypedArray()))

    constructor(vararg sentences: Pair<Sentence, CircumstanceMap>) :
        this(sentences.toList().toMap().toPersistentMap())
}

data class AllowanceQuestion(
    val sentence: Sentence,
    val circumstances: CircumstanceMap = Companion.empty,
) : ConfisQuery {
    constructor(sentence: Sentence, purpose: Purpose) :
        this(sentence, Companion.of(PurposePolicy(purpose)))
}

/**
 * Question meant to represent _'Under what circumstances may A do X?'_
 */
@JvmInline
value class CircumstanceQuestion(val s: Sentence) : ConfisQuery
