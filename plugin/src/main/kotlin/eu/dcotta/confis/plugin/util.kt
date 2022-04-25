package eu.dcotta.confis.plugin

import com.intellij.openapi.vfs.VirtualFile
import com.intellij.util.Alarm
import eu.dcotta.confis.model.Agreement
import eu.dcotta.confis.render.renderMarkdown
import eu.dcotta.confis.scripting.eu.dcotta.confis.scripting.ConfisSourceCode
import kotlin.script.experimental.api.ResultWithDiagnostics
import kotlin.script.experimental.api.ResultWithDiagnostics.Failure
import kotlin.script.experimental.api.ResultWithDiagnostics.Success

fun <R, S> ResultWithDiagnostics<R>.map(transform: (R) -> S): ResultWithDiagnostics<S> = when (this) {
    is Success -> ResultWithDiagnostics.Success(transform(value), reports)
    is Failure -> this
}

inline fun Alarm.request(delayMillis: Long = 0, crossinline action: () -> Unit) = addRequest({ action() }, delayMillis)

fun VirtualFile.asConfisSourceCode(text: String) = ConfisSourceCode(url, name, text)

fun ResultWithDiagnostics<Agreement>.renderMarkdownResult() = when (this) {
    is Success -> value.renderMarkdown()
    is Failure -> reportsAsMarkdown()
}

private fun Failure.reportsAsMarkdown(): String =
    reports.joinToString(separator = "\n\n", prefix = "```\nErrors where found:\n", postfix = "\n```") {
        it.render()
    }
