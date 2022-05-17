package eu.dcotta.confis.plugin

import com.intellij.openapi.diagnostic.logger
import eu.dcotta.confis.dsl.AgreementBuilder
import eu.dcotta.confis.model.Agreement
import eu.dcotta.confis.scripting.ConfisScriptDefinition
import kotlin.script.experimental.api.ResultWithDiagnostics
import kotlin.script.experimental.api.ResultWithDiagnostics.Success
import kotlin.script.experimental.api.SourceCode
import kotlin.script.experimental.api.baseClass
import kotlin.script.experimental.jvm.dependenciesFromClassloader
import kotlin.script.experimental.jvm.jvm
import kotlin.script.experimental.jvmhost.BasicJvmScriptingHost
import kotlin.script.experimental.jvmhost.createJvmCompilationConfigurationFromTemplate

class ConfisHost(private val defaultHost: BasicJvmScriptingHost) {

    private val compilationConfiguration = createJvmCompilationConfigurationFromTemplate<ConfisScriptDefinition> {
        jvm {
            dependenciesFromClassloader(classLoader = ConfisHost::class.java.classLoader, wholeClasspath = true)
            // updateClasspath(getJarForClass(
            //    AgreementBuilder::class,
            //    ConfisScriptDefinition::class,
            //    PersistentList::class,
            // ))
        }
        baseClass(ConfisScriptDefinition::class)
    }

    fun eval(script: SourceCode): ResultWithDiagnostics<Agreement> =
        rawEval(script).map { AgreementBuilder.assemble(it) }

    fun rawEval(script: SourceCode): ResultWithDiagnostics<ConfisScriptDefinition> {
        Thread.currentThread().contextClassLoader = ConfisHost::class.java.classLoader
        val res = defaultHost.eval(script, compilationConfiguration, null)
        if (res !is Success) {
            res.reports.forEach {
                logger<ConfisHost>().warn(it.message, it.exception)
            }
        }

        return res.map {
            val scriptInstance = it.returnValue.scriptInstance
            if (scriptInstance is Throwable) throw IllegalStateException("Confis compilation failure", scriptInstance)
            (scriptInstance as ConfisScriptDefinition)
        }
    }
}
