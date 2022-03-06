package eu.dcotta.confis.eval

import eu.dcotta.confis.eval.CanonisationResult.ContradictionError
import eu.dcotta.confis.eval.CanonisationResult.Success
import eu.dcotta.confis.model.Allowance
import eu.dcotta.confis.model.Allowance.Allow
import eu.dcotta.confis.model.Allowance.Forbid
import eu.dcotta.confis.model.Circumstance
import eu.dcotta.confis.model.Clause.EncodedSentence
import eu.dcotta.confis.model.Purpose
import eu.dcotta.confis.model.Sentence
import eu.dcotta.confis.util.filterValuesNotNull

sealed interface CanonisationResult {
    data class ContradictionError(val contradictions: Map<Atom, List<EncodedSentence>>) : CanonisationResult
    data class Success(val atoms: Map<Atom, Allowance>, val repeats: Map<Atom, List<EncodedSentence>>) :
        CanonisationResult
}
data class Atom(val sentence: Sentence, val purpose: Purpose?, val exception: Circumstance?)

fun canoniseSentences(sentences: List<EncodedSentence>): CanonisationResult {
    val atoms = sentences.flatMap { c -> canonifyClause(c).map { (allowance, atom) -> Triple(allowance, atom, c) } }

    data class AllowanceClause(val allowance: Allowance, val clause: EncodedSentence)

    val indexedByAtom = atoms.groupBy { (_, atom, _) -> atom }
        .mapValues { (_, v) ->
            v.map { (allowance, _, original) -> AllowanceClause(allowance, original) }
        }

    val nonUnique = indexedByAtom.filter { (_, clauses) -> clauses.size > 1 }
    val contradictions = nonUnique
        .filter { (_, clauses) -> clauses.map { it.allowance }.distinct().size != 1 }
        .mapValues { (_, list) -> list.map { it.clause } }

    return if (contradictions.isNotEmpty())
        ContradictionError(contradictions)
    else {

        val repeats = nonUnique.mapValues { (_, v) -> v.map { it.clause } }

        Success(
            atoms = indexedByAtom.mapValues { (_, v) -> v.firstOrNull()?.allowance }.filterValuesNotNull(),
            repeats = repeats
        )
    }
}

fun canonifyClause(clause: EncodedSentence): List<Pair<Allowance, Atom>> {
    val purposes = clause.purposes.ifEmpty { listOf(null) }
    val exceptions = clause.exceptions.ifEmpty { listOf(null) }
    return purposes.flatMap { purposePolicy ->
        exceptions.flatMap { _ ->
            val allowed = when {
                clause.rule.allowance == Forbid -> Forbid
                purposePolicy == null -> Allow
                else -> purposePolicy.allowance
            }

            val policiesOrNull = purposePolicy?.purposes ?: listOf(null)

            policiesOrNull.map { p ->
                // TODO deal with exceptions and circumstances?
                val atom = Atom(clause.rule.sentence, p, null)
                allowed to atom
            }
        }
    }
}
