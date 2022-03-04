package eu.dcotta.confis.model

enum class Allowance { Allow, Forbid }

interface Obj {
    val name: String
}

data class NamedObject(override val name: String) : Obj

interface Subject

data class Action(val name: String)

data class Party(override val name: String) : Subject, Obj

data class Sentence(val subject: Subject, val allowance: Allowance, val action: Action, val obj: Obj) : Clause
