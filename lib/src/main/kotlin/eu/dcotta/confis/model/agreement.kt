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

    data class Allow(val purposes: List<Purpose>) : PurposePolicy {
        constructor(vararg purposes: Purpose) : this(purposes.asList())
    }

    data class Forbid(val purposes: List<Purpose>) : PurposePolicy {
        constructor(vararg purposes: Purpose) : this(purposes.asList())
    }
}
