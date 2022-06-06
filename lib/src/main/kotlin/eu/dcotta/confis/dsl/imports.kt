package eu.dcotta.confis.dsl

import eu.dcotta.confis.model.circumstance.Date
import eu.dcotta.confis.model.circumstance.Month
import eu.dcotta.confis.model.circumstance.MonthDate
import java.util.Calendar

infix fun Int.of(month: Month) = MonthDate(this, month)
infix fun MonthDate.year(year: Int): Date = Date(day, month, year)
//    Calendar.Builder().apply {
//    setDate(year, month.ordinal, day)
// }.build()

infix fun Pair<MonthDate, MonthDate>.year(year: Int) = first.year(year)..second.year(year)

operator fun MonthDate.rangeTo(end: MonthDate) = this to end

data class Days(val i: Int)

data class Months(val i: Int)

data class Years(val i: Int)

fun Date.addingField(field: Int, amount: Int) = Date(cal.apply { add(field, amount) })

operator fun Date.plus(d: Days) = addingField(Calendar.DATE, d.i)
operator fun Date.plus(d: Months) = addingField(Calendar.MONTH, d.i)
operator fun Date.plus(d: Years) = addingField(Calendar.YEAR, d.i)

val Int.days get() = Days(this)
val Int.months get() = Months(this)
val Int.years get() = Years(this)
