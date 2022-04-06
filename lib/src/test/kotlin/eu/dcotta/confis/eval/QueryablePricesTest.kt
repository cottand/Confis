package eu.dcotta.confis.eval

import eu.dcotta.confis.model.Agreement
import io.kotest.core.spec.style.StringSpec

class QueryablePricesTest : StringSpec({
    "can define actions that involve payments" {
        val l = Agreement {
            val alice by party
            val bob by party
            val shareDataWith by action

            alice may { shareDataWith(bob) } asLongAs {
                // TODO
            }
        }
    }
})
