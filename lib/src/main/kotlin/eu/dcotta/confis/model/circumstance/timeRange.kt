package eu.dcotta.confis.model.circumstance

import eu.dcotta.confis.model.circumstance.Circumstance.Key
import eu.dcotta.confis.model.circumstance.TimeRange.OpenFutureRange
import eu.dcotta.confis.model.circumstance.TimeRange.Range
import kotlinx.serialization.Serializable
import java.text.SimpleDateFormat
import java.util.Calendar

sealed interface TimeRange : OverlappingCircumstance {

    @Serializable
    data class Range(override val start: Date, override val endInclusive: Date) :
        ClosedRange<Date>,
        TimeRange,
        Circumstance,
        OverlappingCircumstance {

        override fun contains(other: TimeRange) = when (other) {
            is Date -> other in this
            is OpenFutureRange -> false
            is Range -> other.start in this && other.endInclusive in this
        }

        override fun overlapsWith(other: Circumstance) = other is TimeRange && when (other) {
            is Date -> other in this
            is OpenFutureRange -> other.start <= endInclusive
            is Range -> start in other || endInclusive in other
        }

        override fun toString(): String = "from $start to $endInclusive inclusive"
    }
    override fun generalises(other: Circumstance) = other is TimeRange && this.contains(other)

    operator fun contains(other: TimeRange): Boolean

    @Serializable
    override val key: Circumstance.Key<*> get() = Key

    @Serializable
    companion object Key : Circumstance.Key<TimeRange>

    @Serializable
    data class OpenFutureRange(val start: Date) : TimeRange, OverlappingCircumstance {
        override fun contains(other: TimeRange): Boolean = when (other) {
            is Date -> other >= start
            is OpenFutureRange -> other.start >= start
            is Range -> other.start >= start
        }

        override fun overlapsWith(other: Circumstance): Boolean = other is TimeRange && when (other) {
            is Date -> other >= start
            is OpenFutureRange -> true
            is Range -> other.endInclusive >= start
        }
    }
}

@Serializable
enum class Month {
    January,
    February,
    March,
    April,
    May,
    June,
    July,
    August,
    September,
    October,
    November,
    December;
}

private fun calendarZero() = Calendar.getInstance().apply { clear() }

data class MonthDate(val day: Int, val month: Month)

@Serializable
data class Date(val day: Int, val month: Month, val year: Int) :
    Comparable<Date>,
    Circumstance,
    TimeRange {

    constructor(c: Calendar) :
        this(c.get(Calendar.DATE), Month.values()[c.get(Calendar.MONTH)], c.get(Calendar.YEAR))

    val cal: Calendar
        get() = calendarZero().apply {
            set(year, month.ordinal, day)
        }

    override fun compareTo(other: Date): Int = when {
        year.compareTo(other.year) != 0 -> year.compareTo(other.year)
        month.compareTo(other.month) != 0 -> month.compareTo(other.month)
        day.compareTo(other.day) != 0 -> day.compareTo(other.day)
        else -> 0
    }

    operator fun rangeTo(end: Date) = Range(this, end)
    override fun contains(other: TimeRange): Boolean = when (other) {
        is Date -> other == this
        is Range -> false
        is OpenFutureRange -> false
    }

    override fun overlapsWith(other: Circumstance): Boolean = other is TimeRange &&
        when (other) {
            is Date -> other == this
            is Range -> other.start == this && other.endInclusive == this
            is OpenFutureRange -> other.start <= this
        }

    override fun toString(): String {
        val f = SimpleDateFormat("dd/MM/yyyy")

        return f.format(cal.time)
    }
}
