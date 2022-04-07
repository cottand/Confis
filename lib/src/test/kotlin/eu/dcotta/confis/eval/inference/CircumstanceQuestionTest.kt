package eu.dcotta.confis.eval.inference

import eu.dcotta.confis.asOrFail
import eu.dcotta.confis.model.Action
import eu.dcotta.confis.model.Agreement
import eu.dcotta.confis.model.Clause
import eu.dcotta.confis.model.Party
import eu.dcotta.confis.model.Sentence
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
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
})
