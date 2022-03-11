package eu.dcotta.confis.dsl

import eu.dcotta.confis.model.Action
import eu.dcotta.confis.model.Agreement
import eu.dcotta.confis.model.Allowance
import eu.dcotta.confis.model.Allowance.Allow
import eu.dcotta.confis.model.Allowance.Forbid
import eu.dcotta.confis.model.CircumstanceMap
import eu.dcotta.confis.model.Clause
import eu.dcotta.confis.model.Clause.Rule
import eu.dcotta.confis.model.Clause.SentenceWithCircumstances
import eu.dcotta.confis.model.Obj
import eu.dcotta.confis.model.Obj.Anything
import eu.dcotta.confis.model.Party
import eu.dcotta.confis.model.Purpose
import eu.dcotta.confis.model.PurposePolicy
import eu.dcotta.confis.model.Sentence
import eu.dcotta.confis.model.Subject
import eu.dcotta.confis.util.oneTimeProperty
import kotlin.properties.ReadOnlyProperty

@ConfisDsl
open class AgreementBuilder {

    private val freeTextClauses = mutableListOf<Clause.Text>()
    private val sentencesWithoutCircumstances = mutableListOf<Rule>()
    private val clausesWithCircumstances = mutableListOf<CircumstanceBuilder>()
    internal val parties = mutableListOf<Party>()

    operator fun String.unaryMinus() {
        freeTextClauses += Clause.Text(this.trimIndent())
    }

    infix fun Subject.may(init: SentenceBuilder.() -> Sentence): Rule {
        val rule = Rule(Allow, init(SentenceBuilder(this)))
        sentencesWithoutCircumstances += rule
        return rule
    }

    infix fun Subject.mayNot(init: SentenceBuilder.() -> Sentence): Rule {
        val rule = Rule(Forbid, init(SentenceBuilder(this)))
        sentencesWithoutCircumstances += rule
        return rule
    }

    infix fun Rule.asLongAs(init: CircumstanceBuilder.() -> Unit) {
        val b = CircumstanceBuilder(this, Allow).also(init)
        sentencesWithoutCircumstances.remove(this)
        clausesWithCircumstances += b
    }

    infix fun Rule.unless(init: CircumstanceBuilder.() -> Unit) {
        val b = CircumstanceBuilder(this, Forbid).also(init)
        sentencesWithoutCircumstances.remove(this)
        clausesWithCircumstances += b
    }

    private fun build(): Agreement = Agreement(
        clauses = clausesWithCircumstances.map { it.build() } + sentencesWithoutCircumstances + freeTextClauses,
        parties = parties
    )

    companion object Builder {
        operator fun invoke(builder: AgreementBuilder.() -> Unit) = AgreementBuilder().apply(builder).build()
        fun assemble(builder: AgreementBuilder) = builder.build()
    }
}

fun AgreementBuilder.declareParty(name: String) = oneTimeProperty<Any?, Party> {
    val party = Party(name)
    parties.add(party)
    party
}

val AgreementBuilder.declareObject get() = oneTimeProperty<Any?, Obj> { Obj.Named(it.name) }

val AgreementBuilder.declareParty
    get() = oneTimeProperty<Any?, Party> {
        val party = Party(it.name)
        parties.add(party)
        party
    }

@Suppress("unused")
val AgreementBuilder.declareAction
    get() = ReadOnlyProperty<Any?, Action> { _, prop ->
        Action(prop.name)
    }

class SentenceBuilder(private val subject: Subject) {
    infix operator fun Action.invoke(obj: Obj) = Sentence(subject, this, obj)
    operator fun Action.invoke() = Sentence(subject, this, Anything)
}

@ConfisDsl
class CircumstanceBuilder(private val rule: Rule, private val circumstanceAllowance: Allowance) {
    private val purposePolicies = mutableListOf<Purpose>()
    data class PurposeNarrower(val purposes: List<Purpose>)

    fun include(vararg purposes: Purpose) = PurposeNarrower(purposes.asList())

    object PurposeReceiver

    infix fun PurposeReceiver.purpose(purpose: Purpose) {
        purposePolicies += purpose
    }

    val with = PurposeReceiver

    internal fun build(): SentenceWithCircumstances =
        SentenceWithCircumstances(rule, circumstanceAllowance, CircumstanceMap.of(PurposePolicy(purposePolicies)))
}

@DslMarker
annotation class ConfisDsl
