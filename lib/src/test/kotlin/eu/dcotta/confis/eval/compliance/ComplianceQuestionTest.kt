package eu.dcotta.confis.eval.compliance

import eu.dcotta.confis.Sentence
import eu.dcotta.confis.eval.compliance.ComplianceResult.FullyCompliant
import eu.dcotta.confis.model.Agreement
import eu.dcotta.confis.model.Allowance.Forbid
import eu.dcotta.confis.model.Circumstance
import eu.dcotta.confis.model.CircumstanceMap
import eu.dcotta.confis.model.Clause
import eu.dcotta.confis.model.Party
import eu.dcotta.confis.model.PrecedentSentence
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class ComplianceQuestionTest : StringSpec({

    val alicePaysBob = Sentence { "alice"("pay", Party("bob")) }

    fun compliantIf(vararg cs: Circumstance) = ComplianceResult.CompliantIf(setOf(CircumstanceMap.of(*cs)))

    "fully compliant for empty contract" {
        val a = Agreement {}

        a.ask(ComplianceQuestion()) shouldBe FullyCompliant
    }

    "compliance requirement for requirement clause" {
        val a = Agreement {
            val alice by party
            val bob by party
            val pay by action

            alice must pay(bob)
        }

        a.ask(ComplianceQuestion()) shouldBe compliantIf(PrecedentSentence(alicePaysBob))
    }

    "compliance fulfilled for requirement clause" {
        val a = Agreement {
            val alice by party
            val bob by party
            val pay by action

            alice must pay(bob)
        }

        a.ask(ComplianceQuestion(PrecedentSentence(alicePaysBob))) shouldBe FullyCompliant
    }

    "compliance impossible for breached permission clause" {
        val a = Agreement {
            val alice by party
            val bob by party
            val pay by action

            alice mayNot pay(bob)
        }

        a.ask(ComplianceQuestion(PrecedentSentence(alicePaysBob))) shouldBe ComplianceResult.ComplianceImpossible(
            listOf(
                Clause.Permission(Forbid, alicePaysBob)
            )
        )
    }

    "compliant if not breached permission clause" {
        val a = Agreement {
            val alice by party
            val bob by party
            val pay by action

            alice mayNot pay(bob)
        }

        a.ask(ComplianceQuestion()) shouldBe FullyCompliant
    }
})
