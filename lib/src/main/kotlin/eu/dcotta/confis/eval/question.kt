package eu.dcotta.confis.eval

import eu.dcotta.confis.eval.CanonicalAgreement.Atom
import eu.dcotta.confis.eval.CanonisationResult.ContradictionError
import eu.dcotta.confis.eval.CanonisationResult.Success
import eu.dcotta.confis.model.Action
import eu.dcotta.confis.model.Agreement
import eu.dcotta.confis.model.Allowance
import eu.dcotta.confis.model.Allowance.Allow
import eu.dcotta.confis.model.Allowance.Forbid
import eu.dcotta.confis.model.Circumstance
import eu.dcotta.confis.model.Clause.Encoded
import eu.dcotta.confis.model.Obj
import eu.dcotta.confis.model.Purpose
import eu.dcotta.confis.model.Rule
import eu.dcotta.confis.model.Sentence
import eu.dcotta.confis.model.Subject
import eu.dcotta.confis.util.filterValuesNotNull

data class Question(
    val subject: Subject,
    val action: Action,
    val obj: Obj,
    val purpose: Purpose? = null,
)

sealed interface CanonisationResult {
    data class ContradictionError(val contradictions: Map<Atom, List<Encoded>>) : CanonisationResult
    data class Success(val atoms: Map<Atom, Allowance>, val repeats: Map<Atom, List<Encoded>>) :
        CanonisationResult {
        val value by lazy { CanonicalAgreement(atoms) }
    }
}
class CanonicalAgreement(val atoms: Map<Atom, Allowance>) {
    data class Atom(val sentence: Sentence, val purpose: Purpose?, val exception: Circumstance?)

    fun Agreement.evaluate(q: Question): Allowance {
        val relevantClauses = clauses.filterIsInstance<Encoded>().filter { q relatesTo it.rule }
        TODO()
    }

    infix fun Question.relatesTo(s: Rule) =
        subject == s.subject && action == s.action && obj == s.obj

    companion object {
        /**
         * Construct atoms for each clause and report contradictions
         */
        fun build(agreement: Agreement): CanonisationResult {
            val atoms = agreement.clauses.filterIsInstance<Encoded>().flatMap { clause ->
                val purposes = clause.purposes.ifEmpty { listOf(null) }
                val exceptions = clause.exceptions.ifEmpty { listOf(null) }
                purposes.flatMap { purposePolicy ->
                    exceptions.flatMap { _ ->
                        val allowed = when {
                            clause.rule.allowance == Forbid -> Forbid
                            purposePolicy == null -> Allow
                            else -> purposePolicy.allowance
                        }

                        val policiesOrNull = purposePolicy?.purposes ?: listOf(null)

                        policiesOrNull.map { p ->
                            // TODO deal with exceptions and circumstances
                            val atom = Atom(clause.rule.sentence, p, null)
                            Triple(allowed, atom, clause)
                        }
                    }
                }
            }

            data class AllowanceClause(val allowance: Allowance, val clause: Encoded)

            val indexedByAtom = atoms.groupBy { (_, atom, _) -> atom }
                .mapValues { (_, v) ->
                    v.map { (allowance, _, original) ->
                        AllowanceClause(allowance, original)
                    }
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
    }
}
