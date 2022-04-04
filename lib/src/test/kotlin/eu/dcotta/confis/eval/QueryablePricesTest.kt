package eu.dcotta.confis.eval

import io.kotest.core.spec.style.StringSpec

class QueryablePricesTest : StringSpec({
    "can define actions that involve payments" {
        val l = QueryableAgreement {
            val alice by party
            val bob by party
            val shareDataWith by action

            alice may { shareDataWith(bob) } asLongAs {
            }
        }
    }
})
