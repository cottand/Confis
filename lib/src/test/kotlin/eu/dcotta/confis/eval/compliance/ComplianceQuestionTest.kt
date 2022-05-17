package eu.dcotta.confis.eval.compliance

import eu.dcotta.confis.Sentence
import eu.dcotta.confis.asOrFail
import eu.dcotta.confis.dsl.of
import eu.dcotta.confis.dsl.rangeTo
import eu.dcotta.confis.dsl.year
import eu.dcotta.confis.eval.ComplianceQuestion
import eu.dcotta.confis.eval.compliance.ComplianceResult.Breach
import eu.dcotta.confis.eval.compliance.ComplianceResult.CompliantIf
import eu.dcotta.confis.eval.compliance.ComplianceResult.FullyCompliant
import eu.dcotta.confis.eval.compliance.ComplianceResult.PossibleBreach
import eu.dcotta.confis.model.Agreement
import eu.dcotta.confis.model.Allowance.Forbid
import eu.dcotta.confis.model.CircumstanceMap
import eu.dcotta.confis.model.Clause
import eu.dcotta.confis.model.Obj
import eu.dcotta.confis.model.Party
import eu.dcotta.confis.model.Sentence
import eu.dcotta.confis.model.circumstance.Month.December
import eu.dcotta.confis.model.circumstance.Month.January
import eu.dcotta.confis.model.circumstance.Month.May
import eu.dcotta.confis.model.circumstance.PrecedentSentence
import eu.dcotta.confis.model.circumstance.Purpose.Commercial
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.maps.shouldHaveSize
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.beOfType
import kotlinx.collections.immutable.persistentHashMapOf

class ComplianceQuestionTest : StringSpec({

    val alicePaysBob = Sentence { "alice"("pay", Party("bob")) }
    val may = (1 of May)..(30 of May) year 2022
    val jan = (1 of January)..(30 of January) year 2022
    val year2022 = (1 of January)..(31 of December) year 2022

    fun compliantIf(vararg cs: Sentence) =
        CompliantIf(persistentHashMapOf(*cs.map { it to CircumstanceMap.empty }.toTypedArray()))

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

        a.ask(ComplianceQuestion()) shouldBe compliantIf(alicePaysBob)
    }

    "compliance requirement for requirement with Circumstances clause" {
        val a = Agreement {
            val alice by party
            val bob by party
            val pay by action

            alice must pay(bob) underCircumstances { within { jan } }
        }

        a.ask(ComplianceQuestion()) shouldBe CompliantIf(
            persistentHashMapOf(alicePaysBob to CircumstanceMap.of(jan))
        )
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

        a.ask(ComplianceQuestion(alicePaysBob)) shouldBe Breach(
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
            .asOrFail<Breach>()
    }

    "compliant dubious if past actions overlap but do not generalise" {
        val a = Agreement {
            val alice by party
            val bob by party
            val pay by action

            alice mayNot pay(bob) asLongAs { within { may } }
        }

        a.ask(ComplianceQuestion(alicePaysBob to CircumstanceMap.of(year2022)))
            .asOrFail<PossibleBreach>()
    }

    "non compliant for mayNot..unless permission outside of exception" {
        val a = Agreement {
            val alice by party
            val bob by party
            val pay by action

            alice mayNot pay(bob) unless { within { may } }
        }

        val b = a.ask(ComplianceQuestion(alicePaysBob to CircumstanceMap.of(jan)))
            .asOrFail<Breach>()

        b.clausesBreached shouldHaveSize 1
        b.clausesPossiblyBreached shouldBe emptyList()
    }

    "compliant for mayNot..unless permission inside of exception" {
        val a = Agreement {
            val alice by party
            val bob by party
            val pay by action

            alice mayNot pay(bob) unless { within { may } }
        }

        a.ask(ComplianceQuestion(alicePaysBob to CircumstanceMap.of(may))).asOrFail<FullyCompliant>()
    }

    "breach for mayNot..unless for partly inside exception" {
        val a = Agreement {
            val alice by party
            val bob by party
            val pay by action

            alice mayNot pay(bob) unless { within { may } }
        }

        a.ask(ComplianceQuestion(alicePaysBob to CircumstanceMap.of(year2022))).asOrFail<PossibleBreach>()
    }

    "complex contract edge cases" {
        val a = Agreement {
            val alice by party
            val bob by party

            val payLicenseFeeTo by action
            val use by action
            val share by action

            val data by thing

            bob must payLicenseFeeTo(alice) underCircumstances {
                within { jan }
            }

            alice must share(data) underCircumstances {
                after { bob did payLicenseFeeTo(alice) }
            }

            bob may use(data) asLongAs {
                after { bob did payLicenseFeeTo(alice) }
            }

            bob mayNot use(data) asLongAs {
                with purpose Commercial
            }
        }

        val r = a.ask(ComplianceQuestion()).asOrFail<CompliantIf>()

        r.requirements shouldHaveSize 2

        r.requirements[Sentence { "alice"("share", Obj("data")) }] shouldBe CircumstanceMap.of(
            PrecedentSentence(Sentence { "bob"("payLicenseFeeTo", Party("alice")) })
        )

        r.requirements[Sentence { "bob"("payLicenseFeeTo", Party("alice")) }] shouldBe CircumstanceMap.of(jan)
    }

    "transitivity in past events" {
        val a = Agreement {
            val alice by party
            val bob by party
            val pay by action
            val eat by action
            val cake by thing

            alice must pay(bob) underCircumstances {
                after { alice did eat(cake) }
            }

            alice may eat(cake) asLongAs {
                after { bob did eat(cake) }
            }
        }

        val r = a.ask(
            ComplianceQuestion(Sentence { "alice"("eat", Obj("cake")) }, Sentence { "bob"("eat", Obj("cake")) }, alicePaysBob)
        )

        r should beOfType<FullyCompliant>()
    }
})
