package eu.dcotta.confis.model

data class Agreement(
    val clauses: List<Clause>,
)

sealed interface LegalException {

    object ForceMajeure : LegalException
}

sealed interface Clause {
    @JvmInline
    value class Text(val string: String) : Clause

    data class WithExceptions(val clause: Clause, val exception: List<LegalException>) : Clause

    @JvmInline
    value class PurposePolicies(val policies: List<PurposePolicy>) : Clause {
        constructor(vararg policies: PurposePolicy) : this(policies.asList())
    }
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
