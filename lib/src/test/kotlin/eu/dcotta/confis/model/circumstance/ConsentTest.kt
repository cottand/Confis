package eu.dcotta.confis.model.circumstance

import eu.dcotta.confis.Sentence
import eu.dcotta.confis.model.CircumstanceMap
import eu.dcotta.confis.model.Obj
import eu.dcotta.confis.model.Party
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class ConsentTest : StringSpec({
    val alice = Party("alice")
    val bob = Party("bob")

    val alicePaysBob = Sentence { "alice"("pay", bob) }
    val aliceEatsCookie = Sentence { "alice"("pay", Obj("cookie")) }

    "can have several consents in a circumstance map" {
        val small = CircumstanceMap.of(Consent(alicePaysBob, from = bob))
        val large = CircumstanceMap.of(Consent(alicePaysBob, from = alice)) + small

        (CircumstanceMap.empty generalises small shouldBe true)
        (small generalises large) shouldBe true
        (large generalises small) shouldBe false
    }

    "two unrelated sentences never generalise" {
        val cookie = CircumstanceMap.of(Consent(aliceEatsCookie, from = alice))
        val paying = CircumstanceMap.of(Consent(alicePaysBob, from = alice))

        (cookie generalises paying) shouldBe false
        (paying generalises cookie) shouldBe false
    }
})
