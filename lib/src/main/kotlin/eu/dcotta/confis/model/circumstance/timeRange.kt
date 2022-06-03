package eu.dcotta.confis.model.circumstance

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
        override fun generalises(other: Circumstance) = other is TimeRange && when (other) {
            is Range -> other in this
        }

        @Serializable
        override val key: Circumstance.Key<*> = Key

        override fun contains(other: TimeRange) = other is Range && other.start in this && other.endInclusive in this

        override fun overlapsWith(other: Circumstance) = other is Range && (start in other || endInclusive in other)

        override fun toString(): String = "from $start to $endInclusive inclusive"
    }

    operator fun contains(other: TimeRange): Boolean

    @Serializable
    companion object Key : Circumstance.Key<TimeRange>
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

data class MonthDate(val day: Int, val month: Month)
@Serializable
data class Date(val day: Int, val month: Month, val year: Int) : Comparable<Date> {
    override fun compareTo(other: Date): Int = when {
        year.compareTo(other.year) != 0 -> year.compareTo(other.year)
        month.compareTo(other.month) != 0 -> month.compareTo(other.month)
        day.compareTo(other.day) != 0 -> day.compareTo(other.day)
        else -> 0
    }
    operator fun rangeTo(end: Date) = Range(this, end)

    override fun toString(): String {
        val cal = Calendar.getInstance().apply {
            set(year, month.ordinal, day)
        }

        val f = SimpleDateFormat("dd/MM/yyyy")

        return f.format(cal.time)
    }
}
