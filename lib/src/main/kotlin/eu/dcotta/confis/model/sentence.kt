package eu.dcotta.confis.model

import eu.dcotta.confis.eval.QueryResponse
import eu.dcotta.confis.model.AllowanceResult.Depends
import eu.dcotta.confis.model.Obj.Anything
import eu.dcotta.confis.model.circumstance.WorldState

enum class Allowance {
    Allow, Forbid;

    val asResult get() = if (this == Allow) AllowanceResult.Allow else AllowanceResult.Forbid
}

enum class AllowanceResult : QueryResponse {
    Allow, Forbid, Unspecified, Depends;

    override fun render(): String = toString()
}

/**
 * Returns [Depends] unless [l] equals [r]
 */
fun computeAmbiguous(l: AllowanceResult, r: AllowanceResult) = if (l == r) l else Depends

sealed interface Obj {
    fun render(): String

    class Named(val name: String, val description: String? = null) : Obj {
        override fun toString() = render()
        override fun equals(other: Any?) = other === this || other is Named && name == other.name
        override fun hashCode(): Int = name.hashCode() * 7
        override fun render() = name
    }

    object Anything : Obj {
        override fun render() = ""
    }
}

fun Obj(named: String, description: String? = null) = Obj.Named(named, description)

interface Subject {
    fun render(): String
}

class Action(val name: String, val description: String? = null) {
    override fun toString() = render()
    override fun equals(other: Any?) = other === this || other is Action && name == other.name
    override fun hashCode(): Int = name.hashCode() * 71
    fun render() = name
}

class Party(val name: String, val description: String? = null) : Subject, Obj {
    override fun toString() = render()
    override fun equals(other: Any?) = other === this || other is Party && name == other.name
    override fun hashCode(): Int = name.hashCode() * 71
    override fun render() = name
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
