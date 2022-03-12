package eu.dcotta.confis.model

sealed interface TimeRange {
    data class Range(override val start: Date, override val endInclusive: Date) : ClosedRange<Date>
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
}

object DateBuilder {

    infix fun Int.of(month: Month) = MonthDate(this, month)
    infix fun MonthDate.year(year: Int) = Date(day, month, year)
    infix fun Pair<MonthDate, MonthDate>.year(year: Int) = 2

    operator fun Date.rangeTo(end: Date) = this to end
    operator fun MonthDate.rangeTo(end: MonthDate) = this to end
}
