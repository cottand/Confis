package eu.dcotta.confis.eval

import eu.dcotta.confis.dsl.of
import eu.dcotta.confis.dsl.rangeTo
import eu.dcotta.confis.dsl.year
import eu.dcotta.confis.model.Action
import eu.dcotta.confis.model.AllowanceResult.Depends
import eu.dcotta.confis.model.CircumstanceMap
import eu.dcotta.confis.model.Date
import eu.dcotta.confis.model.Month.June
import eu.dcotta.confis.model.Month.May
import eu.dcotta.confis.model.Month.September
import eu.dcotta.confis.model.Obj
import eu.dcotta.confis.model.Party
import eu.dcotta.confis.model.Sentence
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class QueryableTimeRangesTest : StringSpec({

    val aliceEatsCake = Sentence(Party("alice"), Action("eat"), Obj("cake"))
    val may = 1 of May year 2020
    val june = 1 of June year 2020
    val sept = 1 of September year 2020

    fun QueryableAgreement.ask(s: Sentence, time: Date) =
        ask(AllowanceQuestion(s, CircumstanceMap.of(time..time)))

    "allow asLongAs" {
        val a = QueryableAgreement {

            val alice by party
            val eat by action
            val cake by thing

            alice may { eat(cake) } asLongAs {
                within { may..june }
            }
        }

        a.ask(AllowanceQuestion(aliceEatsCake)) shouldBe Depends
        // a.ask(aliceEatsCake, may) shouldBe Allow
        // a.ask(aliceEatsCake, sept) shouldBe Unspecified
    }
})
