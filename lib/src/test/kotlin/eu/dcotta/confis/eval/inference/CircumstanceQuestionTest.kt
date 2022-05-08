package eu.dcotta.confis.eval.inference

import eu.dcotta.confis.asOrFail
import eu.dcotta.confis.dsl.of
import eu.dcotta.confis.dsl.year
import eu.dcotta.confis.eval.inference.CircumstanceResult.UnderCircumstances
import eu.dcotta.confis.model.Action
import eu.dcotta.confis.model.Agreement
import eu.dcotta.confis.model.CircumstanceMap
import eu.dcotta.confis.model.Clause
import eu.dcotta.confis.model.Party
import eu.dcotta.confis.model.Sentence
import eu.dcotta.confis.model.circumstance.Consent
import eu.dcotta.confis.model.circumstance.Month.December
import eu.dcotta.confis.model.circumstance.Month.January
import eu.dcotta.confis.model.circumstance.Month.May
import eu.dcotta.confis.model.circumstance.Purpose.Commercial
import eu.dcotta.confis.model.circumstance.Purpose.Research
import eu.dcotta.confis.model.circumstance.PurposePolicy
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.beOfType
import kotlin.time.Duration.Companion.seconds

class CircumstanceQuestionTest : StringSpec({

    val alicePayBob = Sentence(Party("alice"), Action("pay"), Party("bob"))
    val bobPayAlice = Sentence(Party("bob"), Action("pay"), Party("alice"))

    "detects the simplest contradiction".config(timeout = 1.seconds) {
        val a = Agreement {
            val alice by party
            val bob by party
            val pay by action

            alice may { pay(bob) }
            alice mayNot { pay(bob) }
        }

        val r = a.ask(CircumstanceQuestion(alicePayBob))

        r.asOrFail<CircumstanceResult.Contradictory>().should {
            // 1 contradiction involving 2 clauses
            it.contradictions shouldHaveSize 1
            it.contradictions.first() shouldHaveSize 2
            val (may, mayNot) = it.contradictions.first()

            may.asOrFail<Clause.Permission>().sentence shouldBe mayNot.asOrFail<Clause.Permission>().sentence
        }
    }

    "circumstance result for rule clauses, allowed" {
        val a = Agreement {
            val alice by party
            val bob by party
            val pay by action

            alice may { pay(bob) }
        }

        val r = a.ask(CircumstanceQuestion(alicePayBob)).asOrFail<CircumstanceResult.UnderCircumstances>()

        r.circumstances shouldBe setOf(CircumstanceMap.empty)
    }

    "irrelevant questions are impossible" {
        val a = Agreement {
            val alice by party
            val bob by party
            val pay by action

            bob may { pay(alice) }
        }

        a.ask(CircumstanceQuestion(alicePayBob)) should beOfType<CircumstanceResult.NotAllowed>()
    }

    "circumstance result for rule clauses, not allowed" {
        val a = Agreement {
            val alice by party
            val bob by party
            val pay by action

            alice mayNot { pay(bob) }
        }

        a.ask(CircumstanceQuestion(alicePayBob)) should beOfType<CircumstanceResult.NotAllowed>()
    }

    "circumstance result for circumstance allow-allow" {
        val a = Agreement {
            val alice by party
            val bob by party
            val pay by action

            alice may { pay(bob) } asLongAs {
                with purpose Commercial
            }
        }

        val r = a.ask(CircumstanceQuestion(alicePayBob)).asOrFail<CircumstanceResult.UnderCircumstances>()

        r.circumstances shouldBe setOf(CircumstanceMap.of(PurposePolicy(Commercial)))
    }

    "circumstance result for circumstance allow-allow (disjunction)" {
        val a = Agreement {
            val alice by party
            val bob by party
            val pay by action

            alice may { pay(bob) } asLongAs {
                with purpose Commercial
            }

            alice may { pay(bob) } asLongAs {
                with purpose Research
            }
        }

        val r = a.ask(CircumstanceQuestion(alicePayBob)).asOrFail<CircumstanceResult.UnderCircumstances>()

        r.circumstances shouldBe setOf(
            CircumstanceMap.of(PurposePolicy(Commercial)),
            CircumstanceMap.of(PurposePolicy(Research)),
        )
    }

    "contradiction detection for rule and circumstance clause allow-allow" {
        val a = Agreement {
            val alice by party
            val bob by party
            val pay by action

            alice may { pay(bob) } asLongAs {
                with purpose Commercial
            }
            alice mayNot { pay(bob) }
        }

        val r = a.ask(CircumstanceQuestion(alicePayBob))

        r.asOrFail<CircumstanceResult.Contradictory>().should {
            // 1 contradiction involving 2 clauses
            it.contradictions shouldHaveSize 1
            it.contradictions.first() shouldHaveSize 2
            val (may, mayNot) = it.contradictions.first()

            val mayClause = may.asOrFail<Clause.PermissionWithCircumstances>()
            mayClause.circumstances shouldBe CircumstanceMap.of(PurposePolicy(Commercial))

            val mayNotPermission = mayNot.asOrFail<Clause.Permission>()

            mayNotPermission.sentence shouldBe mayClause.permission.sentence
        }
    }

    "circumstance result for circumstance allow-forbid" {
        val a = Agreement {
            val alice by party
            val bob by party
            val pay by action

            alice may { pay(bob) } unless { with purpose Commercial }
        }

        val r = a.ask(CircumstanceQuestion(alicePayBob)).asOrFail<CircumstanceResult.UnderCircumstances>()

        r.circumstances shouldBe setOf(
            CircumstanceMap.empty,
        )

        r.unless shouldBe setOf(
            CircumstanceMap.of(PurposePolicy(Commercial)),
        )
    }

    "circumstance result for circumstance forbid-allow" {
        val a = Agreement {
            val alice by party
            val bob by party
            val pay by action

            alice mayNot { pay(bob) } asLongAs { with purpose Commercial }
        }

        a.ask(CircumstanceQuestion(alicePayBob)).asOrFail<CircumstanceResult.NotAllowed>()
    }

    "contradiction detected for forbid-allow" {
        val a = Agreement {
            val alice by party
            val bob by party
            val pay by action

            alice mayNot { pay(bob) } asLongAs { with purpose Commercial }
            alice may { pay(bob) }
        }

        val r = a.ask(CircumstanceQuestion(alicePayBob)).asOrFail<CircumstanceResult.Contradictory>()

        r.contradictions shouldHaveSize 1
        r.contradictions.first() shouldHaveSize 2
    }

    "rules not hit" {
        val a = Agreement {
            val alice by party
            val bob by party
            val pay by action

            alice mayNot { pay(bob) } asLongAs { with purpose Commercial }
            alice may { pay(bob) } asLongAs { with purpose Commercial }
            alice mayNot { pay(bob) } unless { with purpose Commercial }
            alice may { pay(bob) } unless { with purpose Commercial }
        }

        a.ask(CircumstanceQuestion(bobPayAlice)).asOrFail<CircumstanceResult.NotAllowed>()
    }

    "circumstance result for forbid-forbid" {
        val a = Agreement {
            val alice by party
            val bob by party
            val pay by action

            alice mayNot { pay(bob) } unless { with purpose Commercial }
        }

        a.ask(CircumstanceQuestion(alicePayBob)).asOrFail<CircumstanceResult.NotAllowed>()
    }

    "contradiction detection for forbid-forbid" {
        fun verifyAgreementForContradiction(a: Agreement) {
            val r = a.ask(CircumstanceQuestion(alicePayBob)).asOrFail<CircumstanceResult.Contradictory>()
            r.contradictions shouldHaveSize 1
            r.contradictions.first() shouldHaveSize 2
        }

        val a = Agreement {
            val alice by party
            val bob by party
            val pay by action

            alice mayNot { pay(bob) } unless { with purpose Commercial }

            alice may { pay(bob) } unless { with purpose Commercial }
        }

        verifyAgreementForContradiction(a)

        val may2022 = (1 of May year 2022)..(30 of May year 2022)
        val `2022` = (1 of January year 2022)..(30 of December year 2022)

        val b = Agreement {
            val alice by party
            val bob by party
            val pay by action

            alice mayNot { pay(bob) } unless { within { `2022` } }

            alice may { pay(bob) } asLongAs { within { may2022 } }
        }
        b.ask(CircumstanceQuestion(alicePayBob)).asOrFail<UnderCircumstances>() should {
            it.circumstances shouldBe setOf(CircumstanceMap.of(may2022))
            it.unless shouldBe emptySet()
        }

        val b2 = Agreement {
            val alice by party
            val bob by party
            val pay by action

            alice mayNot { pay(bob) } unless { within { may2022 } }

            alice may { pay(bob) } asLongAs { within { may2022 } }
        }

        b2.ask(CircumstanceQuestion(alicePayBob)).asOrFail<UnderCircumstances>() should {
            it.circumstances shouldBe setOf(CircumstanceMap.of(may2022))
            it.unless shouldBe emptySet()
        }

        val c = Agreement {
            val alice by party
            val bob by party
            val pay by action

            alice mayNot { pay(bob) } unless { within { may2022 } }

            alice may { pay(bob) } asLongAs { within { `2022` } }
        }

        verifyAgreementForContradiction(c)
    }

    "requirement clauses are equivalent to allow-allow clauses" {
        val a = Agreement {
            val alice by party
            val bob by party
            val pay by action

            alice must pay(bob)
        }

        a.ask(CircumstanceQuestion(alicePayBob)).asOrFail<UnderCircumstances>() should {
            it.circumstances shouldBe setOf(CircumstanceMap.empty)
            it.unless shouldBe emptySet()
        }

        val may = (1 of May year 2022)..(30 of May year 2022)
        val b = Agreement {
            val alice by party
            val bob by party
            val pay by action

            alice must pay(bob) underCircumstances {
                within { may }
            }
        }

        b.ask(CircumstanceQuestion(alicePayBob)).asOrFail<UnderCircumstances>() should {
            it.circumstances shouldBe setOf(CircumstanceMap.of(may))
            it.unless shouldBe emptySet()
        }
    }

    "permission with consent circumstance" {
        val a = Agreement {
            val alice by party
            val bob by party
            val pay by action

            alice may pay(bob) asLongAs {
                with consentFrom alice
            }
        }

        a.ask(CircumstanceQuestion(alicePayBob)).asOrFail<UnderCircumstances>() should {
            it.circumstances shouldBe setOf(CircumstanceMap.of(Consent(alicePayBob, Party("alice"))))
            it.unless shouldBe emptySet()
        }
    }
})
