package eu.dcotta.confis.model

data class Agreement(
    val clauses: List<Clause>,
    val parties: List<Party>,
)

sealed interface Circumstance {

    object ForceMajeure : Circumstance
}

sealed interface Clause {
    @JvmInline
    value class Text(val string: String) : Clause

    data class Encoded(
        val rule: Rule,
        val purposes: List<PurposePolicy> = emptyList(),
        val exceptions: List<Circumstance> = emptyList(),
    ) : Clause
}

enum class Purpose { Commercial, Research }

sealed interface PurposePolicy {

    val purposes: List<Purpose>
    val allowance: Allowance

    data class Allow(override val purposes: List<Purpose>) : PurposePolicy {
        override val allowance get() = Allowance.Allow
        constructor(vararg purposes: Purpose) : this(purposes.asList())
    }

    data class Forbid(override val purposes: List<Purpose>) : PurposePolicy {
        override val allowance get() = Allowance.Forbid
        constructor(vararg purposes: Purpose) : this(purposes.asList())
    }
}
