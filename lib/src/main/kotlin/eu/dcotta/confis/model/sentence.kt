package eu.dcotta.confis.model

enum class Allowance {
    Allow, Forbid;

    val asResult get() = if (this == Allow) AllowanceResult.Allow else AllowanceResult.Forbid
}

sealed interface AllowanceResult {
    object Allow : AllowanceResult
    object Forbid : AllowanceResult
    object Unspecified: AllowanceResult
    data class AllowForPurposes(val purposes: List<Purpose>) : AllowanceResult

    // operator fun minus(others: List<Purpose>) = when(this) {
    //    Allow -> this
    //    is AllowForPurposes -> copy(purposes - others)
    //    Forbid -> AllowForPurposes(others)
    // }

    // not commutative - the RHS allows more freedom
    // TODO decide semantics of contract and which queries we will support
    infix fun with(other: AllowanceResult) =  when (this) {
        Allow -> Allow
        is AllowForPurposes -> TODO()
        Forbid -> when (other) {
            Allow -> TODO()
            is AllowForPurposes -> TODO()
            Forbid -> TODO()
            Unspecified -> TODO()
        }
        Unspecified -> TODO()
    }
}

interface Obj {
    data class Named(val name: String) : Obj
    object Anything : Obj
}

interface Subject

data class Action(val name: String)

data class Party(val name: String) : Subject, Obj

data class Rule(val allowance: Allowance, val sentence: Sentence) {
    val subject by sentence::subject
    val obj by sentence::obj
    val action by sentence::action
}

data class Sentence(val subject: Subject, val action: Action, val obj: Obj)
