package eu.dcotta.confis.eval

import eu.dcotta.confis.dsl.LicenseBuilder
import eu.dcotta.confis.dsl.declareAction
import eu.dcotta.confis.dsl.declareParty
import eu.dcotta.confis.eval.CanonicalAgreement.Atom
import eu.dcotta.confis.eval.CanonisationResult.ContradictionError
import eu.dcotta.confis.eval.CanonisationResult.Success
import eu.dcotta.confis.model.Action
import eu.dcotta.confis.model.Allowance.Allow
import eu.dcotta.confis.model.Allowance.Forbid
import eu.dcotta.confis.model.Party
import eu.dcotta.confis.model.Purpose.Commercial
import eu.dcotta.confis.model.Purpose.Research
import eu.dcotta.confis.model.Sentence
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.collections.shouldStartWith
import io.kotest.matchers.maps.shouldHaveSize
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Assertions.fail

class CanonicalAgreementTest : StringSpec({
    val anaHugsBob = Sentence(Party("ana"), Action("hug"), Party("bob"))
    val anaHugsBobForResearch = Atom(anaHugsBob, Research, exception = null)
    val anaHugsBobForProfit = Atom(anaHugsBob, Commercial, exception = null)

    "detects contradiction in separate clauses" {
        val bad = LicenseBuilder {
            val ana by declareParty
            val bob by declareParty
            val hug by declareAction

            ana may { hug(bob) }
            ana mayNot { hug(bob) }
        }

        val result = CanonicalAgreement.build(bad) as? ContradictionError
            ?: fail("Expected failure")

        result.contradictions should { atoms ->
            atoms.size shouldBe 1
            atoms.entries.first().should { (atom, clauses) ->
                atom shouldBe Atom(sentence = anaHugsBob, purpose = null, exception = null)

                clauses shouldHaveSize 2

                clauses.forEach { it.rule.sentence shouldBe anaHugsBob }
            }
        }
    }

    "contradiction in purposes" {
        val bad = LicenseBuilder {
            val ana by declareParty
            val bob by declareParty
            val hug by declareAction

            ana may { hug(bob) } additionally {
                purposes allowed include(Commercial)
                purposes forbidden include(Commercial)
            }
        }
        val result = CanonicalAgreement.build(bad) as? ContradictionError
            ?: fail("Expected failure")

        result.contradictions[anaHugsBobForProfit] should {
            it?.size shouldBe 2
            it?.forEach { clause ->
                clause.rule.sentence shouldBe anaHugsBob
            }
        }
    }

    // TODO decide on semantics of this stuff
    "contradiction in purposes vs other clause" {
        val bad = LicenseBuilder {
            val ana by declareParty
            val bob by declareParty
            val hug by declareAction

            ana may { hug(bob) } additionally {
                purposes allowed include(Commercial)
            }
            ana mayNot { hug(bob) }
        }
        val result = CanonicalAgreement.build(bad) as? ContradictionError
            ?: fail("Expected failure")

        result.contradictions should { atoms ->
            atoms.size shouldBe 2
        }
    }

    "simple agreement" {
        val good = LicenseBuilder {
            val ana by declareParty
            val bob by declareParty
            val hug by declareAction

            ana may { hug(bob) }
            bob may { hug(ana) }
        }

        val result = CanonicalAgreement.build(good) as? Success
            ?: fail("Should succeed")

        result.atoms shouldHaveSize 2
        result.repeats shouldBe emptyMap()

        result.atoms.values shouldStartWith listOf(Allow, Allow)
    }

    "can deal with purposes" {
        val good = LicenseBuilder {
            val ana by declareParty
            val bob by declareParty
            val hug by declareAction

            ana may { hug(bob) } additionally {
                purposes allowed include(Research)
                purposes forbidden include(Commercial)
            }
        }

        val result = CanonicalAgreement.build(good) as? Success
            ?: fail("Should succeed")

        result.atoms shouldHaveSize 2
        result.repeats shouldBe emptyMap()

        result.atoms[anaHugsBobForResearch] shouldBe Allow
        result.atoms[anaHugsBobForProfit] shouldBe Forbid
    }
})
