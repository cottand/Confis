package eu.dcotta.confis.dsl

import eu.dcotta.confis.model.Date
import eu.dcotta.confis.model.Month
import eu.dcotta.confis.model.MonthDate
import eu.dcotta.confis.model.TimeRange

infix fun Int.of(month: Month) = MonthDate(this, month)
infix fun MonthDate.year(year: Int): Date = Date(day, month, year)
//    Calendar.Builder().apply {
//    setDate(year, month.ordinal, day)
// }.build()

infix fun Pair<MonthDate, MonthDate>.year(year: Int) = first.year(year) to second.year(year)

operator fun Date.rangeTo(end: Date) = TimeRange.Range(this, end)
operator fun MonthDate.rangeTo(end: MonthDate) = this to end
