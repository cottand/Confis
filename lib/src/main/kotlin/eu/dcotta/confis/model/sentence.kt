package eu.dcotta.confis.model

import eu.dcotta.confis.model.Obj.Anything

enum class Allowance {
    Allow, Forbid;

    val asResult get() = if (this == Allow) AllowanceResult.Allow else AllowanceResult.Forbid
}

enum class AllowanceResult {
    Allow, Forbid, Unspecified;

    // takes the least permissive
    infix fun and(other: AllowanceResult): AllowanceResult = when (this) {
        Allow -> when (other) {
            Allow -> Allow
            Forbid -> Forbid
            Unspecified -> Allow
        }
        Forbid -> this
        Unspecified -> other
    }
}

interface Obj {
    data class Named(val name: String) : Obj
    object Anything : Obj

    companion object {
        operator fun invoke(name: String) = Named(name)
    }
}

interface Subject

data class Action(val name: String)

data class Party(val name: String) : Subject, Obj

data class Sentence(val subject: Subject, val action: Action, val obj: Obj) {
    /**
     * whether [this] is a more general version of [other]
     */
    operator fun contains(other: Sentence): Boolean = this == other ||
        (subject == other.subject && action == other.action && obj == Anything)
}
