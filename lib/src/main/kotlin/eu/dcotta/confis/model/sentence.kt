package eu.dcotta.confis.model

enum class Allowance {
    Allow, Forbid;

    val result get() = AllowanceResult.valueOf(name)
}

enum class AllowanceResult { Allow, Forbid, Unspecified }

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
