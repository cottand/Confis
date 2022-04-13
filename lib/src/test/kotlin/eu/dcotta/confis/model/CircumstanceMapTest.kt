package eu.dcotta.confis.model

import eu.dcotta.confis.dsl.of
import eu.dcotta.confis.dsl.year
import eu.dcotta.confis.model.Circumstance.Key
import eu.dcotta.confis.model.Month.May
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

    val may = (1 of May year 2022)..(30 of May year 2022)

    "empty circumstance map is disjoint from nothing and overlaps with all" {
        CircumstanceMap.empty.overlapsWith(CircumstanceMap.of(may)) shouldBe true
        CircumstanceMap.empty.disjoint(CircumstanceMap.of(may)) shouldBe false
    }

    "time periods overlap" {
        val may1 = CircumstanceMap.of((1 of May year 2022)..(20 of May year 2022))
        val may2 = CircumstanceMap.of((10 of May year 2022)..(30 of May year 2022))

        may1.overlapsWith(may2) shouldBe true
        may2.overlapsWith(may1) shouldBe true

        may1.disjoint(may2) shouldBe false
        may2.disjoint(may1) shouldBe false
    }
})
