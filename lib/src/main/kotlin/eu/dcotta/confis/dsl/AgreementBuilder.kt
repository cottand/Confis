package eu.dcotta.confis.dsl

import eu.dcotta.confis.model.Action
import eu.dcotta.confis.model.Agreement
import eu.dcotta.confis.model.Allowance.Allow
import eu.dcotta.confis.model.Allowance.Forbid
import eu.dcotta.confis.model.Clause.Rule
import eu.dcotta.confis.model.Clause.Text
import eu.dcotta.confis.model.Obj
import eu.dcotta.confis.model.Obj.Named
import eu.dcotta.confis.model.Party
import eu.dcotta.confis.model.Sentence
import eu.dcotta.confis.model.Subject
import eu.dcotta.confis.util.oneTimeProperty
import eu.dcotta.confis.util.removeLastOccurrence
import kotlin.properties.ReadOnlyProperty

@ConfisDsl
open class AgreementBuilder {

    private val freeTextClauses = mutableListOf<Text>()
    private val sentencesWithoutCircumstances = mutableListOf<Rule>()
    private val clausesWithCircumstances = mutableListOf<CircumstanceBuilder>()
    private val parties = mutableListOf<Party>()

    operator fun String.unaryMinus() {
        freeTextClauses += Text(this.trimIndent())
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
        sentencesWithoutCircumstances.removeLastOccurrence(this)
        clausesWithCircumstances += b
    }

    infix fun Rule.unless(init: CircumstanceBuilder.() -> Unit) {
        val b = CircumstanceBuilder(this, Forbid).also(init)
        sentencesWithoutCircumstances.removeLastOccurrence(this)
        clausesWithCircumstances += b
    }

    private fun build(): Agreement = Agreement(
        clauses = clausesWithCircumstances.map { it.build() } + sentencesWithoutCircumstances + freeTextClauses,
        parties = parties
    )

    fun party(named: String) = oneTimeProperty<Any?, Party> {
        val party = Party(named)
        parties.add(party)
        party
    }

    val thing get() = oneTimeProperty<Any?, Obj> { Named(it.name) }

    val party
        get() = oneTimeProperty<Any?, Party> {
            val party = Party(it.name)
            parties.add(party)
            party
        }

    @Suppress("unused")
    val declareAction = ReadOnlyProperty<Any?, Action> { _, prop ->
        Action(prop.name)
    }

    companion object Builder {
        operator fun invoke(builder: AgreementBuilder.() -> Unit) = AgreementBuilder().apply(builder).build()
        fun assemble(builder: AgreementBuilder) = builder.build()
    }
}
