package eu.dcotta.confis.dsl

import eu.dcotta.confis.model.Clause
import eu.dcotta.confis.model.Date
import eu.dcotta.confis.model.Month.May
import eu.dcotta.confis.model.Purpose.Commercial
import eu.dcotta.confis.model.TimeRange
import io.kotest.assertions.fail
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class TimeCircumstanceBuilderTest : StringSpec({

    "can define timerange" {
        val a = AgreementBuilder {

            val engineeringTeamMember by party
            val cake by thing

            val eat by action

            engineeringTeamMember may { eat(cake) } asLongAs {
                within { (1 of May year 2022)..(3 of May year 2022) }
                with purpose Commercial
            }
            val startOfContract = 1 of May year 2021
            val endOfContract = 3 of May year 2023

            engineeringTeamMember may { eat(cake) } asLongAs {
                within { startOfContract..endOfContract }
            }
        }

        val c = a.clauses.first()
        c as? Clause.PermissionWithCircumstances ?: fail("should be clause")

        c.circumstances[TimeRange] shouldBe Date(1, May, 2022)..Date(3, May, 2022)
    }
})
