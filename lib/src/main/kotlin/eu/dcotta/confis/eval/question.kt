package eu.dcotta.confis.eval

import eu.dcotta.confis.model.Action
import eu.dcotta.confis.model.Agreement
import eu.dcotta.confis.model.Allowance
import eu.dcotta.confis.model.Circumstance
import eu.dcotta.confis.model.Clause.Encoded
import eu.dcotta.confis.model.Obj
import eu.dcotta.confis.model.Purpose
import eu.dcotta.confis.model.Rule
import eu.dcotta.confis.model.Sentence
import eu.dcotta.confis.model.Subject

data class Question(
    val subject: Subject,
    val action: Action,
    val obj: Obj,
    val purpose: Purpose? = null,
)

class CanonicalAgreement {
    data class Atom(val sentence: Sentence, val purpose: Purpose?, val exception: Circumstance?)

    /**
     * Construct atoms for each clause and report contradictions
     */
    // fun build(agreement: Agreement) {
    //    val atoms = agreement.clauses.filterIsInstance<Clause.Encoded>() .flatMap {  clause ->
    //        clause.purposes.flatMap { purpose ->
    //            clause.exceptions.flatMap { exception ->
    //                Atom(clause.rule.sentence, )
    //            }
    //        }
    //    }
    // }
}

fun Agreement.canonicalForm() {
}

fun Agreement.evaluate(q: Question): Allowance {
    val relevantClauses = clauses.filterIsInstance<Encoded>().filter { q relatesTo it.rule }
    TODO()
}

infix fun Question.relatesTo(s: Rule) =
    subject == s.subject && action == s.action && obj == s.obj
