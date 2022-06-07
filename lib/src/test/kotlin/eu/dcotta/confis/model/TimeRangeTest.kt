package eu.dcotta.confis.model

import eu.dcotta.confis.dsl.days
import eu.dcotta.confis.dsl.months
import eu.dcotta.confis.dsl.of
import eu.dcotta.confis.dsl.plus
import eu.dcotta.confis.dsl.year
import eu.dcotta.confis.dsl.years
import eu.dcotta.confis.model.circumstance.Month.January
import eu.dcotta.confis.model.circumstance.Month.June
import eu.dcotta.confis.model.circumstance.Month.May
import eu.dcotta.confis.model.circumstance.Month.September
import eu.dcotta.confis.model.circumstance.TimeRange
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

    "no circumstances is more general than a time range" {

        (CircumstanceMap.empty generalises CircumstanceMap.of(may..may)) shouldBe true
    }

    "can sum days in DSL" {
        may.plus(31.days) shouldBe june
    }

    "can sum months in DSL" {
        may.plus(1.months) shouldBe june
    }

    "can sum years in DSL" {
        ((1 of January year 2020) + 1.years) shouldBe (1 of January year 2021)
    }

    "cannot store both timeRange and instant in a circumstanceMap" {
        (CircumstanceMap.of(may..sept) + may) shouldBe CircumstanceMap.of(may)
    }

    "cannot store openRange and timeRange in a circumstanceMap" {
        (CircumstanceMap.of(TimeRange.OpenFutureRange(may)) + (may..sept)) shouldBe
            CircumstanceMap.of(may..sept)
    }

    "TimeRange generalises date" {
        ((may..sept) generalises may) shouldBe true
        ((may..sept) generalises sept) shouldBe true
        ((may..sept) generalises june) shouldBe true
        ((may..june) generalises sept) shouldBe false

        (may generalises (may..sept)) shouldBe false
    }

    "OpenFutureRange generalises Date, TimeRange" {
        (TimeRange.OpenFutureRange(may) generalises may) shouldBe true
        (TimeRange.OpenFutureRange(may) generalises june) shouldBe true
        (TimeRange.OpenFutureRange(june) generalises may) shouldBe false
        (TimeRange.OpenFutureRange(june) generalises (may..june)) shouldBe false
        (TimeRange.OpenFutureRange(june) overlapsWith (may..june)) shouldBe true
        (TimeRange.OpenFutureRange(june) overlapsWith (june..sept)) shouldBe true
        (TimeRange.OpenFutureRange(june) generalises (june..sept)) shouldBe true

        (may generalises TimeRange.OpenFutureRange(may)) shouldBe false
        ((june..sept) generalises TimeRange.OpenFutureRange(may)) shouldBe false
        ((may..sept) generalises TimeRange.OpenFutureRange(june)) shouldBe false

        (may overlapsWith TimeRange.OpenFutureRange(may)) shouldBe true
        ((june..sept) overlapsWith TimeRange.OpenFutureRange(may)) shouldBe true
        ((may..sept) overlapsWith TimeRange.OpenFutureRange(june)) shouldBe true
    }
})
