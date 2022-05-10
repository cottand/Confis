package eu.dcotta.confis.plugin

import com.intellij.openapi.vfs.VirtualFile
import com.intellij.util.messages.Topic
import eu.dcotta.confis.model.Agreement
import kotlin.script.experimental.api.ResultWithDiagnostics

fun interface ConfisCompiledNotifier {
    fun afterCompile(file: VirtualFile, agreement: ResultWithDiagnostics<Agreement>)

    companion object {
        val CHANGE_ACTION_TOPIC = Topic.create("confis topic", ConfisCompiledNotifier::class.java)
    }
}
