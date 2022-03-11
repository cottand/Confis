package eu.dcotta.confis.model

import eu.dcotta.confis.model.Circumstance.Key
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

data class IntCircumstance(val i: Int) : Circumstance {
    override val key = Companion
    override fun generalises(other: Circumstance) = other is IntCircumstance && i >= other.i

    companion object : Key<IntCircumstance>
}

data class StrCircumstance(val i: String) : Circumstance {
    override val key = Companion
    override fun generalises(other: Circumstance) = other is StrCircumstance && i in other.i

    companion object : Key<StrCircumstance>
}

class CircumstanceMapTest : StringSpec({

    "can instantiate and merge" {
        val int = CircumstanceMap.of(IntCircumstance(5))
        val str = CircumstanceMap.of(StrCircumstance("hi"))

        val intKey = IntCircumstance
        val strKey = StrCircumstance

        int.generalises(intKey) shouldBe true
        str.generalises(strKey) shouldBe true

        int.generalises(strKey) shouldBe false
        str.generalises(intKey) shouldBe false

        int.generalises(str) shouldBe false
        str.generalises(int) shouldBe false

        val new = int + str

        new.generalises(intKey) shouldBe true
        new.generalises(strKey) shouldBe true

        new.generalises(strKey) shouldBe true
        new.generalises(intKey) shouldBe true

        new.generalises(str) shouldBe false
        new.generalises(int) shouldBe false
    }

    "empty generalises all" {
        CircumstanceMap.empty generalises CircumstanceMap.of(IntCircumstance(2)) shouldBe true
    }
})
