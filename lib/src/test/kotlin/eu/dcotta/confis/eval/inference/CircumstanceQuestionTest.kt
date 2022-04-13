package eu.dcotta.confis.eval.inference

import eu.dcotta.confis.asOrFail
import eu.dcotta.confis.model.Action
import eu.dcotta.confis.model.Agreement
import eu.dcotta.confis.model.CircumstanceMap
import eu.dcotta.confis.model.Clause
import eu.dcotta.confis.model.Party
import eu.dcotta.confis.model.Purpose.Commercial
import eu.dcotta.confis.model.Purpose.Research
import eu.dcotta.confis.model.PurposePolicy
import eu.dcotta.confis.model.Sentence
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.beOfType
import kotlin.time.Duration.Companion.seconds

class CircumstanceQuestionTest : StringSpec({

    val alicePayBob = Sentence(Party("alice"), Action("pay"), Party("bob"))

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

            may.asOrFail<Clause.Rule>().sentence shouldBe mayNot.asOrFail<Clause.Rule>().sentence
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

        a.ask(CircumstanceQuestion(alicePayBob)) should beOfType<CircumstanceResult.Forbidden>()
    }

    "circumstance result for rule clauses, not allowed" {
        val a = Agreement {
            val alice by party
            val bob by party
            val pay by action

            alice mayNot { pay(bob) }
        }

        a.ask(CircumstanceQuestion(alicePayBob)) should beOfType<CircumstanceResult.Forbidden>()
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

            val mayClause = may.asOrFail<Clause.SentenceWithCircumstances>()
            mayClause.circumstances shouldBe CircumstanceMap.of(PurposePolicy(Commercial))

            val mayNotRule = mayNot.asOrFail<Clause.Rule>()

            mayNotRule.sentence shouldBe mayClause.rule.sentence
        }
    }

    // TODO FIXME
    "circumstance result for circumstance allow-forbid" {
        val a = Agreement {
            val alice by party
            val bob by party
            val pay by action

            alice may { pay(bob) } unless { with purpose Commercial }
        }

        val r = a.ask(CircumstanceQuestion(alicePayBob)).asOrFail<CircumstanceResult.UnderCircumstances>()

        r.circumstances shouldBe setOf(
            // CircumstanceMap.empty,
        )

        r.forbidden shouldBe setOf(
            CircumstanceMap.of(PurposePolicy(Commercial)),
        )
    }
})
