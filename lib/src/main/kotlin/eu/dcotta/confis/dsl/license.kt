package eu.dcotta.confis.dsl

import eu.dcotta.confis.model.Action
import eu.dcotta.confis.model.Agreement
import eu.dcotta.confis.model.Allowance
import eu.dcotta.confis.model.Allowance.Allow
import eu.dcotta.confis.model.Allowance.Forbid
import eu.dcotta.confis.model.Clause
import eu.dcotta.confis.model.LegalException
import eu.dcotta.confis.model.LegalException.ForceMajeure
import eu.dcotta.confis.model.Obj
import eu.dcotta.confis.model.Party
import eu.dcotta.confis.model.Purpose
import eu.dcotta.confis.model.PurposePolicy
import eu.dcotta.confis.model.Sentence
import eu.dcotta.confis.model.Subject
import kotlin.properties.ReadOnlyProperty

@ConfisDsl
open class LicenseBuilder {

    private val clauses = mutableListOf<ClauseBuilder>()
    private val freeTextClauses = mutableListOf<Clause.Text>()
    internal val parties = mutableListOf<Party>()

    operator fun String.unaryMinus() {
        freeTextClauses += Clause.Text(this)
    }

    infix fun Subject.may(init: SentenceBuilder.() -> Sentence): ClauseBuilder {
        val clause = ClauseBuilder(init(SentenceBuilder(this, Allow)))
        clauses += clause
        return clause
    }

    infix fun Subject.mayNot(init: SentenceBuilder.() -> Sentence): ClauseBuilder {
        val clause = ClauseBuilder(init(SentenceBuilder(this, Forbid)))
        clauses += clause
        return clause
    }

    infix fun ClauseBuilder.unless(init: ExceptionBuilder.() -> Unit): ClauseBuilder {
        val e = ExceptionBuilder().apply(init).cause
        if (e != null) this.exceptions += e
        return this
    }

    infix fun ClauseBuilder.additionally(init: ClauseBuilder.() -> Unit): ClauseBuilder = also(init)

    private fun build(): Agreement = Agreement(
        clauses = clauses.map(ClauseBuilder::build) + freeTextClauses,
        parties = parties
    )

    companion object Builder {
        operator fun invoke(builder: LicenseBuilder.() -> Unit) = LicenseBuilder().apply(builder).build()
        fun assemble(builder: LicenseBuilder) = builder.build()
    }
}

fun LicenseBuilder.declareParty(name: String) = oneTimeProperty<Nothing?, Party> {
    val party = Party(name)
    parties.add(party)
    party
}

val LicenseBuilder.declareParty get() = oneTimeProperty<Nothing?, Party> {
    val party = Party(it.name)
    parties.add(party)
    party
}

@Suppress("unused")
val LicenseBuilder.declareAction
    get() = ReadOnlyProperty<Nothing?, Action> { _, prop ->
        Action(prop.name)
    }

@ConfisDsl
class ExceptionBuilder {

    internal var cause: LegalException? = null

    val forceMajeure: Unit
        get() {
            cause = ForceMajeure
        }
}

class SentenceBuilder(private val subject: Subject, private val allowance: Allowance) {
    infix operator fun Action.invoke(obj: Obj) = Sentence(subject, allowance, this, obj)
}

@ConfisDsl
class ClauseBuilder(private val sentence: Sentence) {
    private val purposePolicies = mutableListOf<PurposePolicy>()

    internal val exceptions = mutableListOf<LegalException>()

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

    internal fun build() = Clause.Encoded(sentence, purposePolicies.toList(), exceptions.toList())
}

@DslMarker
annotation class ConfisDsl
