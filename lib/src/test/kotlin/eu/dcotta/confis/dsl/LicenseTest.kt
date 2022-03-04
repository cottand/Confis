package eu.dcotta.confis.dsl

import eu.dcotta.confis.model.Allowance
import eu.dcotta.confis.model.Circumstance.ForceMajeure
import eu.dcotta.confis.model.Clause
import eu.dcotta.confis.model.Party
import eu.dcotta.confis.model.Purpose.Commercial
import eu.dcotta.confis.model.Purpose.Research
import eu.dcotta.confis.model.PurposePolicy.Allow
import eu.dcotta.confis.model.PurposePolicy.Forbid
import io.kotest.assertions.fail
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe

inline fun <reified M> Any.narrowedTo() = if (this is M) this else fail("$this should be of type ${M::class}")

class LicenseTest : StringSpec({

    "can define freetext clause" {

        val l = LicenseBuilder {
            -"This is a freetext clause"
        }

        l.clauses.first() shouldBe Clause.Text("This is a freetext clause")
    }

    "can declare actions" {
        LicenseBuilder {

            val copy by declareAction
        }
    }

    "can declare parties" {
        val l = LicenseBuilder {

            val alice by declareParty
            val bob by declareParty(name = "You!")
        }

        l.parties.shouldContainAll(Party("alice"), Party("You!"))
    }

    "can allow a declared action to a party" {
        val l = LicenseBuilder {
            val alice by declareParty("alice")

            val bob by declareParty("bob")

            val hug by declareAction

            alice may { hug(bob) }
        }

        val sentence = l.clauses.first().narrowedTo<Clause.Encoded>().rule

        sentence should {
            it.action.name shouldBe "hug"
            it.subject shouldBe Party("alice")
            it.obj shouldBe Party("bob")
            it.allowance shouldBe Allowance.Allow
        }
    }

    "can forbid a declared action to a party" {
        val l = LicenseBuilder {
            val alice by declareParty("alice")

            val bob by declareParty("bob")

            val hug by declareAction

            alice mayNot { hug(bob) }
        }

        val sentence = l.clauses.first().narrowedTo<Clause.Encoded>().rule

        sentence should {
            it.action.name shouldBe "hug"
            it.subject shouldBe Party("alice")
            it.obj shouldBe Party("bob")
            it.allowance shouldBe Allowance.Forbid
        }
    }

    "can add a forceManejeure exception to a sentence" {
        val l = LicenseBuilder {
            val alice by declareParty("alice")

            val bob by declareParty("bob")

            val hug by declareAction

            alice mayNot { hug(bob) } unless { forceMajeure }
        }
        val clause = l.clauses.first()

        (clause as? Clause.Encoded) ?: fail("clause should have exceptions")

        clause.exceptions.first() shouldBe ForceMajeure

        clause.rule should {
            it.action.name shouldBe "hug"
            it.subject shouldBe Party("alice")
            it.obj shouldBe Party("bob")
            it.allowance shouldBe Allowance.Forbid
        }
    }

    "add purposes to a sentence" {
        val l = LicenseBuilder {
            val alice by declareParty("alice")

            val bob by declareParty("bob")

            val hug by declareAction

            alice may { hug(bob) } additionally {
                purposes allowed include(Research)
                purposes forbidden include(Commercial)
            }
        }

        val clause = l.clauses.first().narrowedTo<Clause.Encoded>()

        clause.purposes shouldContainAll listOf(Allow(Research), Forbid(Commercial))
    }

    "can chain additionally clauses" {
        val l = LicenseBuilder {
            val alice by declareParty
            val bob by declareParty("bob")
            val hug by declareAction
            alice may { hug(bob) } additionally {
                purposes allowed include(Research)
                purposes forbidden include(Commercial)
            }
        }

        val l2 = LicenseBuilder {
            val alice by declareParty("alice")
            val bob by declareParty("bob")
            val hug by declareAction
            alice may {
                hug(bob)
            } additionally {
                purposes allowed include(Research)
            } additionally {
                purposes forbidden include(Commercial)
            }
        }

        l shouldBe l2
    }
})
