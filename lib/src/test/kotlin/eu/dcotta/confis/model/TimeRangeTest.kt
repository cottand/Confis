package eu.dcotta.confis.model

import eu.dcotta.confis.dsl.of
import eu.dcotta.confis.dsl.rangeTo
import eu.dcotta.confis.dsl.year
import eu.dcotta.confis.model.Month.June
import eu.dcotta.confis.model.Month.May
import eu.dcotta.confis.model.Month.September
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class TimeRangeTest : StringSpec({

    val may = 1 of May year 2020
    val june = 1 of June year 2020
    val sept = 1 of September year 2020

    "larger time range generalises smaller one" {
        (may..sept) generalises (may..june) shouldBe true

        (CircumstanceMap.of(may..sept) generalises CircumstanceMap.of(may..june)) shouldBe true
    }

    "smaller time range does not generalise bigger one" {
        (may..june) generalises (may..sept) shouldBe false
    }

    "equal time ranges generalise each other" {
        (may..sept) generalises (may..sept) shouldBe true
    }

    "a single-point time range is generalised by a large one" {
        (may..sept) generalises (may..may) shouldBe true

        (CircumstanceMap.of(may..sept) generalises CircumstanceMap.of(may..may)) shouldBe true
    }
})
