package eu.dcotta.confis.dsl

import eu.dcotta.confis.model.Allowance
import eu.dcotta.confis.model.Allowance.Allow
import eu.dcotta.confis.model.Allowance.Forbid
import eu.dcotta.confis.model.CircumstanceMap
import eu.dcotta.confis.model.Clause
import eu.dcotta.confis.model.Party
import eu.dcotta.confis.model.Purpose.Commercial
import eu.dcotta.confis.model.Purpose.Research
import eu.dcotta.confis.model.PurposePolicy
import io.kotest.assertions.fail
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe

inline fun <reified M> Any.narrowedTo() = if (this is M) this else fail("$this should be of type ${M::class}")

@Suppress("UNUSED_VARIABLE")
class LicenseTest : StringSpec({

    "can define freetext clause" {

        val l = AgreementBuilder {
            -"This is a freetext clause"
        }

        l.clauses.first() shouldBe Clause.Text("This is a freetext clause")
    }

    "can declare actions" {
        AgreementBuilder {

            val copy by declareAction
        }
    }

    "can declare parties" {
        val l = AgreementBuilder {

            val alice by declareParty
            val bob by declareParty(name = "You!")
        }

        l.parties.shouldContainAll(Party("alice"), Party("You!"))
    }

    "can allow a declared action to a party" {
        val l = AgreementBuilder {
            val alice by declareParty("alice")

            val bob by declareParty("bob")

            val hug by declareAction

            alice may { hug(bob) }
        }

        val sentence = l.clauses.first().narrowedTo<Clause.Rule>()

        sentence should {
            it.action.name shouldBe "hug"
            it.subject shouldBe Party("alice")
            it.obj shouldBe Party("bob")
            it.allowance shouldBe Allowance.Allow
        }
    }

    "can forbid a declared action to a party" {
        val l = AgreementBuilder {
            val alice by declareParty("alice")

            val bob by declareParty("bob")

            val hug by declareAction

            alice mayNot { hug(bob) }
        }

        val sentence = l.clauses.first().narrowedTo<Clause.Rule>()

        sentence should {
            it.action.name shouldBe "hug"
            it.subject shouldBe Party("alice")
            it.obj shouldBe Party("bob")
            it.allowance shouldBe Allowance.Forbid
        }
    }

    "add purposes to a sentence" {
        val l = AgreementBuilder {
            val alice by declareParty("alice")

            val bob by declareParty("bob")

            val hug by declareAction

            alice may { hug(bob) } asLongAs {
                with purpose (Research)
            }

            alice mayNot { hug(bob) } asLongAs {
                with purpose Commercial
            }
        }

        val clauses = l.clauses.filterIsInstance<Clause.SentenceWithCircumstances>()

        val (research, commercial) = clauses

        research should {
            it.circumstances shouldBe CircumstanceMap.of(PurposePolicy(Research))
            it.rule.allowance shouldBe Allow
            it.circumstanceAllowance shouldBe Allow
        }

        commercial should {
            it.circumstances shouldBe CircumstanceMap.of(PurposePolicy(Commercial))
            it.rule.allowance shouldBe Forbid
            it.circumstanceAllowance shouldBe Allow
        }
    }
})
