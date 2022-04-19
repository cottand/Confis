package eu.dcotta.confis.plugin

import kotlin.script.experimental.api.ResultWithDiagnostics
import kotlin.script.experimental.api.ResultWithDiagnostics.Failure
import kotlin.script.experimental.api.ResultWithDiagnostics.Success

fun <R, S> ResultWithDiagnostics<R>.map(transform: (R) -> S): ResultWithDiagnostics<S> = when (this) {
    is Success -> ResultWithDiagnostics.Success(transform(value), reports)
    is Failure -> this
}
