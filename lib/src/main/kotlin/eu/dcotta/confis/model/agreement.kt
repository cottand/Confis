package eu.dcotta.confis.model

data class Agreement(
    val clauses: List<Clause>,
    val parties: List<Party>,
)

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
