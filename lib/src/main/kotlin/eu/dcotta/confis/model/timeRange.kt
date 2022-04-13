package eu.dcotta.confis.model

sealed interface TimeRange : OverlappingCircumstance {

    data class Range(override val start: Date, override val endInclusive: Date) : ClosedRange<Date>, TimeRange {
        override fun generalises(other: Circumstance) = other is TimeRange && when (other) {
            is Range -> other in this
        }

        override val key: Circumstance.Key<*> get() = Key

        override fun contains(other: TimeRange) = other is Range && other.start in this && other.endInclusive in this

        override fun overlapsWith(other: Circumstance) = other is Range && (start in other || endInclusive in other)
    }

    operator fun contains(other: TimeRange): Boolean

    companion object Key : Circumstance.Key<TimeRange>
}

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
data class Date(val day: Int, val month: Month, val year: Int) : Comparable<Date> {
    override fun compareTo(other: Date): Int = when {
        year.compareTo(other.year) != 0 -> year.compareTo(other.year)
        month.compareTo(other.month) != 0 -> month.compareTo(other.month)
        day.compareTo(other.day) != 0 -> day.compareTo(other.day)
        else -> 0
    }
    operator fun rangeTo(end: Date) = TimeRange.Range(this, end)
}
