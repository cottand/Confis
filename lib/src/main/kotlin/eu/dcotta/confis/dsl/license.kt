package eu.dcotta.confis.dsl

import eu.dcotta.confis.model.Agreement
import eu.dcotta.confis.model.Clause
import eu.dcotta.confis.model.LegalException
import eu.dcotta.confis.model.LegalException.ForceMajeure
import eu.dcotta.confis.model.Purpose
import eu.dcotta.confis.model.PurposePolicy

@ConfisDsl
open class LicenseBuilder {

    private val clauses = mutableListOf<ClauseBuilder>()
    private val freeTextClauses = mutableListOf<Clause.Text>()

    operator fun String.unaryMinus() {
        freeTextClauses += Clause.Text(this)
    }

    fun o(init: ClauseBuilder.() -> Unit): ClauseBuilder {
        val built = ClauseBuilder().apply(init)
        clauses += built
        return built
    }

    infix fun ClauseBuilder.unless(init: ExceptionBuilder.() -> Unit): ClauseBuilder {
        val e = ExceptionBuilder().apply(init).cause
        if (e != null) this.exceptions += e
        return this
    }

    private fun build(): Agreement = Agreement(clauses.map(ClauseBuilder::build) + freeTextClauses)

    companion object Builder {
        operator fun invoke(builder: LicenseBuilder.() -> Unit) = LicenseBuilder().apply(builder).build()
        fun assemble(builder: LicenseBuilder) = builder.build()
    }
}

@ConfisDsl
class ExceptionBuilder {

    internal var cause: LegalException? = null

    val forceMajeure: Unit
        get() {
            cause = ForceMajeure
            return Unit
        }
}

@ConfisDsl
class ClauseBuilder {
    internal val purposePolicies = mutableListOf<PurposePolicy>()

    internal var exceptions = mutableListOf<LegalException>()

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

    internal fun build(): Clause {
        val clauseWithoutExceptions = Clause.PurposePolicies(purposePolicies)
        return when {
            exceptions.isNotEmpty() -> Clause.WithExceptions(clauseWithoutExceptions, exceptions.toList())
            else -> clauseWithoutExceptions
        }
    }
}

@DslMarker
annotation class ConfisDsl
