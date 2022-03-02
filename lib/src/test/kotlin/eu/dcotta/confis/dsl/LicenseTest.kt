package eu.dcotta.confis.dsl

import eu.dcotta.confis.model.Clause
import eu.dcotta.confis.model.Clause.PurposePolicies
import eu.dcotta.confis.model.LegalException.ForceMajeure
import eu.dcotta.confis.model.Purpose.Commercial
import eu.dcotta.confis.model.PurposePolicy.Allow
import eu.dcotta.confis.model.PurposePolicy.Forbid
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.beOfType

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
        l.clauses.first() shouldBe PurposePolicies(Allow(Commercial))
    }

    "can forbid commercial purpose in clause" {
        val l = LicenseBuilder {

            o { purposes forbidden include(Commercial) }
        }

        l.clauses.first() shouldBe PurposePolicies(Forbid(Commercial))
    }

    "can forbid commercial purpose in clause unless forceMajeure exception" {
        val l = LicenseBuilder {

            o { purposes forbidden include(Commercial) } unless { forceMajeure }
        }

        val policy = l.clauses.first()
        policy should beOfType<Clause.WithExceptions>()
        (policy as Clause.WithExceptions) should {
            it.clause shouldBe PurposePolicies(Forbid(Commercial))
            it.exception shouldContain ForceMajeure
        }
    }
})
