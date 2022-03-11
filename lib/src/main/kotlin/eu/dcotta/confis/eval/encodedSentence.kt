package eu.dcotta.confis.eval

import eu.dcotta.confis.model.Allowance
import eu.dcotta.confis.model.Circumstance
import eu.dcotta.confis.model.Clause.SentenceWithCircumstances
import eu.dcotta.confis.model.Sentence

sealed interface CanonisationResult {
    data class ContradictionError(val contradictions: Map<Atom, List<SentenceWithCircumstances>>) : CanonisationResult
    data class Success(val atoms: Map<Atom, Allowance>, val repeats: Map<Atom, List<SentenceWithCircumstances>>) :
        CanonisationResult
}

/**
 * [circumstances] is a conjunction
 */
data class Atom(val sentence: Sentence, val circumstances: List<Circumstance>)

// TODO revisit verification!
