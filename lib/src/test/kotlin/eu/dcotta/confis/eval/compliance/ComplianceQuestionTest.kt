package eu.dcotta.confis.eval.compliance

import eu.dcotta.confis.Sentence
import eu.dcotta.confis.asOrFail
import eu.dcotta.confis.dsl.of
import eu.dcotta.confis.dsl.rangeTo
import eu.dcotta.confis.dsl.year
import eu.dcotta.confis.eval.compliance.ComplianceResult.FullyCompliant
import eu.dcotta.confis.model.Agreement
import eu.dcotta.confis.model.Allowance.Forbid
import eu.dcotta.confis.model.Circumstance
import eu.dcotta.confis.model.CircumstanceMap
import eu.dcotta.confis.model.Clause
import eu.dcotta.confis.model.Month.December
import eu.dcotta.confis.model.Month.January
import eu.dcotta.confis.model.Month.May
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

        a.ask(ComplianceQuestion(alicePaysBob)) shouldBe FullyCompliant
    }

    "compliance impossible for breached permission clause" {
        val a = Agreement {
            val alice by party
            val bob by party
            val pay by action

            alice mayNot pay(bob)
        }

        a.ask(ComplianceQuestion(alicePaysBob)) shouldBe ComplianceResult.ComplianceImpossible(
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

    val may = (1 of May)..(30 of May) year 2022
    val year2022 = (1 of January)..(31 of December) year 2022

    "non compliant if breached permission clause" {
        val a = Agreement {
            val alice by party
            val bob by party
            val pay by action

            alice mayNot pay(bob) asLongAs {
                within { year2022 }
            }
        }

        a.ask(ComplianceQuestion(alicePaysBob to CircumstanceMap.of(may)))
            .asOrFail<ComplianceResult.ComplianceImpossible>()
    }

    "compliant dubious if past actions overlap but do not generalise" {
        val a = Agreement {
            val alice by party
            val bob by party
            val pay by action

            alice mayNot pay(bob) asLongAs {
                within { may }
            }
        }

        a.ask(ComplianceQuestion(alicePaysBob to CircumstanceMap.of(year2022)))
            .asOrFail<ComplianceResult.PossibleBreach>()
    }
})
