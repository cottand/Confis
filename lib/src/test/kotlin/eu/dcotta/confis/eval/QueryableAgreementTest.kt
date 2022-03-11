package eu.dcotta.confis.eval

import eu.dcotta.confis.dsl.declareAction
import eu.dcotta.confis.dsl.declareObject
import eu.dcotta.confis.dsl.declareParty
import eu.dcotta.confis.model.Action
import eu.dcotta.confis.model.AllowanceResult.Allow
import eu.dcotta.confis.model.AllowanceResult.Forbid
import eu.dcotta.confis.model.AllowanceResult.Unspecified
import eu.dcotta.confis.model.Obj
import eu.dcotta.confis.model.Party
import eu.dcotta.confis.model.Purpose.Commercial
import eu.dcotta.confis.model.Purpose.Research
import eu.dcotta.confis.model.Sentence
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class QueryableAgreementTest : StringSpec({

    val aliceEatsCake = Sentence(Party("alice"), Action("eat"), Obj("cake"))
    val bobEatsCake = Sentence(Party("bob"), Action("eat"), Obj("cake"))
    val aliceEatsCookie = Sentence(Party("alice"), Action("eat"), Obj("cookie"))

    "can answer simple sentence query" {
        val a = QueryableAgreement {

            val alice by declareParty
            val eat by declareAction
            val cake by declareObject
            val cookie by declareObject

            alice may { eat(cake) }
            alice mayNot { eat(cookie) }
        }

        a.ask(AllowanceQuestion(aliceEatsCake)) shouldBe Allow
        a.ask(AllowanceQuestion(aliceEatsCookie)) shouldBe Forbid
        a.ask(AllowanceQuestion(bobEatsCake)) shouldBe Unspecified
    }

    "can answer on purposes" {
        val a = QueryableAgreement {

            val alice by declareParty
            val eat by declareAction
            val cake by declareObject
            val cookie by declareObject

            alice may { eat(cake) } unless {
                with purpose Commercial
            }

            alice may { eat(cookie) } asLongAs {
                with purpose (Research)
            }
        }

        a.ask(AllowanceQuestion(aliceEatsCake)) shouldBe Unspecified
        a.ask(AllowanceQuestion(aliceEatsCake, purpose = Commercial)) shouldBe Forbid

        a.ask(AllowanceQuestion(aliceEatsCookie)) shouldBe Unspecified
        a.ask(AllowanceQuestion(aliceEatsCookie, purpose = Research)) shouldBe Allow
    }
    "purposes in rules create precedence between them" {
        val a = QueryableAgreement {

            val alice by declareParty
            val eat by declareAction
            val cake by declareObject
            val cookie by declareObject

            alice may { eat(cake) } unless {
                with purpose Commercial
            }

            // alice cannot eat cookies unless it is for research
            alice mayNot { eat(cookie) }
            alice may { eat(cookie) } asLongAs {
                with purpose Research
            }
        }

        a.ask(AllowanceQuestion(aliceEatsCake)) shouldBe Unspecified
        a.ask(AllowanceQuestion(aliceEatsCake, purpose = Commercial)) shouldBe Forbid
        a.ask(AllowanceQuestion(aliceEatsCake, purpose = Research)) shouldBe Allow

        // we cannot say for sure for the general case
        a.ask(AllowanceQuestion(aliceEatsCookie)) shouldBe Unspecified
        a.ask(AllowanceQuestion(aliceEatsCookie, purpose = Research)) shouldBe Allow
    }
})
