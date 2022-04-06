package eu.dcotta.confis.model

import eu.dcotta.confis.dsl.of
import eu.dcotta.confis.dsl.rangeTo
import eu.dcotta.confis.dsl.year
import eu.dcotta.confis.eval.AllowanceQuestion
import eu.dcotta.confis.eval.ask
import eu.dcotta.confis.model.AllowanceResult.Allow
import eu.dcotta.confis.model.AllowanceResult.Depends
import eu.dcotta.confis.model.Clause.SentenceWithCircumstances
import eu.dcotta.confis.model.Month.August
import eu.dcotta.confis.model.Month.December
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class PrecedentSentenceTest : StringSpec({

    val alicePaysBob = Sentence(Party("alice"), Action("pay"), Party("bob"))
    val bobPaysAlice = Sentence(Party("bob"), Action("pay"), Party("alice"))
    val date = 1 of August year 2022
    val onAugust = CircumstanceMap.of(date..date)
    val afterAlicePaidBob = CircumstanceMap.of(PrecedentSentence(alicePaysBob))

    "a precedent sentence circumstance is generalises no precedents" {
        (CircumstanceMap.empty generalises afterAlicePaidBob) shouldBe true
        (afterAlicePaidBob generalises afterAlicePaidBob) shouldBe true
    }

    "can write agreements with precedent sentences" {
        val a = Agreement {
            val alice by party
            val pay by action
            val bob by party

            // bob may pay alice only if alice has paid bob
            // (which cannot happen withing Christmas holidays)

            alice may { pay(bob) } unless {
                within { (24 of December)..(29 of December) year 2022 }
            }

            bob may { pay(alice) } asLongAs {
                alice did { pay(bob) }
            }
        }

        val agreementSentence = (a.clauses[1] as SentenceWithCircumstances).rule.sentence
        (bobPaysAlice generalises agreementSentence) shouldBe true
        (agreementSentence generalises bobPaysAlice) shouldBe true

        a.ask(AllowanceQuestion(alicePaysBob, onAugust)) shouldBe Allow

        // TODO should this perhaps be Forbid or Unspecified? alice did not pay bob so asLongAs says the case is not
        //  specified
        a.ask(AllowanceQuestion(bobPaysAlice)) shouldBe Depends

        a.ask(AllowanceQuestion(bobPaysAlice, afterAlicePaidBob)) shouldBe Allow
    }
})
