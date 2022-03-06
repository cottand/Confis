package eu.dcotta.confis.dsl

import eu.dcotta.confis.model.Action
import eu.dcotta.confis.model.Agreement
import eu.dcotta.confis.model.Allowance.Allow
import eu.dcotta.confis.model.Allowance.Forbid
import eu.dcotta.confis.model.Circumstance
import eu.dcotta.confis.model.Circumstance.ForceMajeure
import eu.dcotta.confis.model.Clause
import eu.dcotta.confis.model.Clause.EncodedSentence
import eu.dcotta.confis.model.Obj
import eu.dcotta.confis.model.Obj.Anything
import eu.dcotta.confis.model.Party
import eu.dcotta.confis.model.Purpose
import eu.dcotta.confis.model.PurposePolicy
import eu.dcotta.confis.model.Rule
import eu.dcotta.confis.model.Sentence
import eu.dcotta.confis.model.Subject
import eu.dcotta.confis.util.oneTimeProperty
import kotlin.properties.ReadOnlyProperty

@ConfisDsl
open class AgreementBuilder {

    private val clauses = mutableListOf<ClauseBuilder>()
    private val freeTextClauses = mutableListOf<Clause.Text>()
    internal val parties = mutableListOf<Party>()

    operator fun String.unaryMinus() {
        freeTextClauses += Clause.Text(this.trimIndent())
    }

    infix fun Subject.may(init: SentenceBuilder.() -> Sentence): ClauseBuilderAllowed {
        val clause = ClauseBuilderAllowed(init(SentenceBuilder(this)))
        clauses += clause
        return clause
    }

    infix fun Subject.mayNot(init: SentenceBuilder.() -> Sentence): ClauseBuilderForbidden {
        val clause = ClauseBuilderForbidden(init(SentenceBuilder(this)))
        clauses += clause
        return clause
    }

    infix fun ClauseBuilder.unless(init: ExceptionBuilder.() -> Unit): ClauseBuilder {
        val e = ExceptionBuilder().apply(init).cause
        if (e != null) this.exceptions += e
        return this
    }

    infix fun ClauseBuilderAllowed.additionally(init: ClauseBuilderAllowed.() -> Unit): ClauseBuilderAllowed =
        also(init)

    private fun build(): Agreement = Agreement(
        clauses = clauses.map(ClauseBuilder::build) + freeTextClauses,
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

@ConfisDsl
class ExceptionBuilder {

    internal var cause: Circumstance? = null

    val forceMajeure: Unit
        get() {
            cause = ForceMajeure
        }
}

class SentenceBuilder(private val subject: Subject) {
    infix operator fun Action.invoke(obj: Obj) = Sentence(subject, this, obj)
    operator fun Action.invoke() = Sentence(subject, this, Anything)
}

/**
 * Accepts exceptions and purposes
 */
@ConfisDsl
class ClauseBuilderAllowed(private val sentence: Sentence) : ClauseBuilder(Rule(Allow, sentence)) {
    internal val purposePolicies = mutableListOf<PurposePolicy>()

    data class PurposeNarrower(val purposes: List<Purpose>)

    fun include(vararg purposes: Purpose) = PurposeNarrower(purposes.asList())

    inner class PurposeReceiver {
        infix fun allowed(narrower: PurposeNarrower) {
            purposePolicies += PurposePolicy.Allow(narrower.purposes)
        }

        infix fun forbidden(narrower: PurposeNarrower) {
            purposePolicies += PurposePolicy.Forbid(narrower.purposes)
        }
    }

    val purposes = PurposeReceiver()
}

/**
 * Only accepts exceptions
 */
@ConfisDsl
class ClauseBuilderForbidden(sentence: Sentence) : ClauseBuilder(Rule(Forbid, sentence))

@ConfisDsl
sealed class ClauseBuilder(private val rule: Rule) {

    internal val exceptions = mutableListOf<Circumstance>()

    internal fun build(): EncodedSentence {
        val purposes = when (this) {
            is ClauseBuilderAllowed -> purposePolicies
            is ClauseBuilderForbidden -> emptyList()
        }
        return Clause.EncodedSentence(rule, purposes, exceptions.toList())
    }
}

@DslMarker
annotation class ConfisDsl
