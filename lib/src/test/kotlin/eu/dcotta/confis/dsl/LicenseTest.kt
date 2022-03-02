package eu.dcotta.confis.dsl

import eu.dcotta.confis.model.Clause
import eu.dcotta.confis.model.Purpose.Commercial
import eu.dcotta.confis.model.PurposePolicy
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class LicenseTest : StringSpec({

    "can define freetext clause" {

        val l = LicenseBuilder {
            -"This is a freetext clause"
        }

        l.clauses.first() shouldBe Clause.Text("This is a freetext clause")
    }

    "can allow commercial purpose in clause" {
        val l = LicenseBuilder {

            o { purposes allowed include(Commercial) }
        }

        l.clauses.first() shouldBe PurposePolicy.Allow(Commercial)
    }
})
