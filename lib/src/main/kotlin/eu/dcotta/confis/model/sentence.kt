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

/**
 * Returns [Depends] unless [l] equals [r]
 */
fun computeAmbiguous(l: AllowanceResult, r: AllowanceResult) = if (l == r) l else Depends

interface Obj {
    class Named(val name: String, val description: String? = null) : Obj {
        override fun toString() = "Obj($name ${description ?: ""})"
        override fun equals(other: Any?) = other === this || other is Named && name == other.name
        override fun hashCode(): Int = name.hashCode() * 7
    }

    object Anything : Obj
}

fun Obj(named: String, description: String? = null) = Obj.Named(named, description)

interface Subject

class Action(val name: String, val description: String? = null) {
    override fun toString() = "Action($name ${description ?: ""})"
    override fun equals(other: Any?) = other === this || other is Action && name == other.name
    override fun hashCode(): Int = name.hashCode() * 71
}

class Party(val name: String, val description: String? = null) : Subject, Obj {
    override fun toString() = "Party($name ${description ?: ""})"
    override fun equals(other: Any?) = other === this || other is Party && name == other.name
    override fun hashCode(): Int = name.hashCode() * 71
}

data class Sentence(val subject: Subject, val action: Action, val obj: Obj) {
    /**
     * whether [this] is a more general version of [other]
     */
    infix fun generalises(other: Sentence): Boolean = this == other ||
        (subject == other.subject && action == other.action && obj == Anything)

    override fun toString(): String = "$subject $action $obj"

    infix fun happenedIn(world: WorldState) = world.containsKey(this)
}
