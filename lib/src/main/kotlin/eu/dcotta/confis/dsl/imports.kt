package eu.dcotta.confis.dsl

import eu.dcotta.confis.model.Date
import eu.dcotta.confis.model.Month
import eu.dcotta.confis.model.MonthDate
import eu.dcotta.confis.model.TimeRange

infix fun Int.of(month: Month) = MonthDate(this, month)
infix fun MonthDate.year(year: Int) = Date(day, month, year)
infix fun Pair<MonthDate, MonthDate>.year(year: Int) = 2

operator fun Date.rangeTo(end: Date) = TimeRange.Range(this, end)
operator fun MonthDate.rangeTo(end: MonthDate) = this to end
