package eu.dcotta.confis.model

import eu.dcotta.confis.model.AllowanceResult.Allow
import eu.dcotta.confis.model.AllowanceResult.Depends
import eu.dcotta.confis.model.AllowanceResult.Forbid
import eu.dcotta.confis.model.AllowanceResult.Unspecified
import eu.dcotta.confis.model.Obj.Anything

enum class Allowance {
    Allow, Forbid;

    val asResult get() = if (this == Allow) AllowanceResult.Allow else AllowanceResult.Forbid
}

enum class AllowanceResult {
    Allow, Forbid, Unspecified, Depends;
}

// takes the least permissive
infix fun AllowanceResult.leastPermissive(other: AllowanceResult): AllowanceResult = when (this) {
    Allow -> when (other) {
        Allow -> Allow
        Forbid -> Forbid
        Unspecified, Depends -> Allow
    }
    Forbid -> Forbid
    Unspecified, Depends -> other
}

fun mostPermissive(left: AllowanceResult, other: AllowanceResult): AllowanceResult = when (left) {
    Forbid -> when (other) {
        Allow -> Allow
        Forbid -> Forbid
        Unspecified, Depends -> Allow
    }
    Allow -> Allow
    Unspecified, Depends -> other
}

fun computeAmbiguous(l: AllowanceResult, r: AllowanceResult) = when {
    l == r -> l
    l == Unspecified -> Depends
    r == Unspecified -> Depends
    else -> Depends
}

interface Obj {
    data class Named(val name: String) : Obj {
        override fun toString() = "Obj($name)"
    }

    object Anything : Obj

    companion object {
        operator fun invoke(name: String) = Named(name)
    }
}

interface Subject

data class Action(val name: String) {
    override fun toString() = "Action($name)"
}

data class Party(val name: String) : Subject, Obj {
    override fun toString() = "Party($name)"
}

data class Sentence(val subject: Subject, val action: Action, val obj: Obj) {
    /**
     * whether [this] is a more general version of [other]
     */
    infix fun generalises(other: Sentence): Boolean = this == other ||
        (subject == other.subject && action == other.action && obj == Anything)

    override fun toString(): String = "$subject $action $obj"
}
