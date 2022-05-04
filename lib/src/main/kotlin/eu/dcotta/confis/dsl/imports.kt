package eu.dcotta.confis.dsl

import eu.dcotta.confis.model.circumstance.Date
import eu.dcotta.confis.model.circumstance.Month
import eu.dcotta.confis.model.circumstance.MonthDate

infix fun Int.of(month: Month) = MonthDate(this, month)
infix fun MonthDate.year(year: Int): Date = Date(day, month, year)
//    Calendar.Builder().apply {
//    setDate(year, month.ordinal, day)
// }.build()

infix fun Pair<MonthDate, MonthDate>.year(year: Int) = first.year(year)..second.year(year)

operator fun MonthDate.rangeTo(end: MonthDate) = this to end
