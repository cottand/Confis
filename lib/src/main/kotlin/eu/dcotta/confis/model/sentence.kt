package eu.dcotta.confis.model

enum class Allowance { Allow, Forbid }

interface Obj {
    val name: String
}

data class NamedObject(override val name: String) : Obj

interface Subject

data class Action(val name: String)

data class Party(override val name: String) : Subject, Obj

data class Rule(val allowance: Allowance, val sentence: Sentence) {
    val subject by sentence::subject
    val obj by sentence::obj
    val action by sentence::action
}

data class Sentence(val subject: Subject, val action: Action, val obj: Obj)
