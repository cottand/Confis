package eu.dcotta.confis.model

import eu.dcotta.confis.model.Circumstance.Key
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
data class IntCircumstance(val i: Int) : Circumstance {
    override val key = Companion
    override fun contains(other: Circumstance) = other is IntCircumstance && i >= other.i
    companion object : Key<IntCircumstance>
}
data class StrCircumstance(val i: String) : Circumstance {
    override val key = Companion
    override fun contains(other: Circumstance) = other is StrCircumstance && i.contains(other.i)
    companion object : Key<StrCircumstance>
}

class CircumstanceMapTest : StringSpec({

    "can instantiate and merge" {
        val int = CircumstanceMap.of(IntCircumstance(5))
        val str = CircumstanceMap.of(StrCircumstance("hi"))

        val intKey = IntCircumstance
        val strKey = StrCircumstance

        int.contains(intKey) shouldBe true
        str.contains(strKey) shouldBe true

        int.contains(strKey) shouldBe false
        str.contains(intKey) shouldBe false

        int.contains(str) shouldBe false
        str.contains(int) shouldBe false

        val new = int + str

        new.contains(intKey) shouldBe true
        new.contains(strKey) shouldBe true

        new.contains(strKey) shouldBe true
        new.contains(intKey) shouldBe true

        new.contains(str) shouldBe true
        new.contains(int) shouldBe true
    }
})
