package eu.dcotta.confis.model

data class Agreement(
    val clauses: List<Clause>
)

sealed interface Clause {
    @JvmInline
   value class Text(val string: String): Clause

}

enum class Purpose { Commercial, Research }


sealed interface PurposePolicy: Clause {

    data class Allow(val purpose: Purpose): PurposePolicy
}
