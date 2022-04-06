package eu.dcotta.confis.model

import eu.dcotta.confis.dsl.AgreementBuilder

data class Agreement(
    val clauses: List<Clause>,
    val parties: List<Party>,
) {
    companion object {
        operator fun invoke(builder: AgreementBuilder.() -> Unit): Agreement = AgreementBuilder(builder)
    }
}

sealed interface Clause {
    @JvmInline
    value class Text(val string: String) : Clause

    data class SentenceWithCircumstances(
        val rule: Rule,
        val circumstanceAllowance: Allowance,
        val circumstances: CircumstanceMap,
    ) : Clause

    data class Rule(val allowance: Allowance, val sentence: Sentence) : Clause {
        val subject by sentence::subject
        val obj by sentence::obj
        val action by sentence::action

        override fun toString() = "Rule($allowance $sentence)"
    }
}
