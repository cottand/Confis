package eu.dcotta.confis.dsl

import eu.dcotta.confis.model.Agreement
import eu.dcotta.confis.model.Clause
import eu.dcotta.confis.model.Purpose
import eu.dcotta.confis.model.PurposePolicy

@ConfisDsl
class LicenseBuilder {

    private val clauses = mutableListOf<Clause>()

    operator fun String.unaryMinus() {
        clauses += Clause.Text(this)
    }

    fun o(init: ClauseBuilder.() -> Unit) {
        clauses += ClauseBuilder().apply(init).purposePolicies
    }

    private fun build(): Agreement = Agreement(clauses)

    companion object Builder {
        operator fun invoke(builder: LicenseBuilder.() -> Unit) = LicenseBuilder().apply(builder).build()
    }
}

@ConfisDsl
class ClauseBuilder {
    internal val purposePolicies = mutableListOf<PurposePolicy>()

    data class PurposeNarrower(val purposes: List<Purpose>)

    fun include(vararg purposes: Purpose) = PurposeNarrower(purposes.asList())

    inner class PurposeReceiver {
        infix fun allowed(narrower: PurposeNarrower) {
            purposePolicies += narrower.purposes.map { PurposePolicy.Allow(it) }
        }
    }

    val purposes = PurposeReceiver()
}

@DslMarker
annotation class ConfisDsl
