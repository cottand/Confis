package eu.dcotta.confis.dsl

import eu.dcotta.confis.model.Month.May
import io.kotest.core.spec.style.StringSpec

class TimeCircumstanceBuilderTest : StringSpec({

    "can define timerange" {
        AgreementBuilder {
            val ana by party
            val cake by thing
            val eat by action

            ana may { eat(cake) } asLongAs {
                within { (1 of May year 2022)..(3 of May year 2022) }
            }
        }
        // c as? Clause.SentenceWithCircumstances ?: fail("should be clause")
        //
        // //c.circumstances[TimeRange] shouldBe Date(1, May, 2022)..Date(3, May, 2022)
    }
})
