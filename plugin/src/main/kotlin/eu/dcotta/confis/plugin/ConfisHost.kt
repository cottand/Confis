package eu.dcotta.confis.plugin

import com.intellij.openapi.diagnostic.logger
import eu.dcotta.confis.dsl.AgreementBuilder
import eu.dcotta.confis.dsl.AgreementBuilder.Builder
import eu.dcotta.confis.model.Agreement
import eu.dcotta.confis.scripting.ConfisScriptDefinition
import kotlin.script.experimental.api.ResultWithDiagnostics
import kotlin.script.experimental.api.ResultWithDiagnostics.Success
import kotlin.script.experimental.api.ScriptAcceptedLocation.Everywhere
import kotlin.script.experimental.api.SourceCode
import kotlin.script.experimental.api.acceptedLocations
import kotlin.script.experimental.api.ide
import kotlin.script.experimental.jvm.dependenciesFromCurrentContext
import kotlin.script.experimental.jvm.jvm
import kotlin.script.experimental.jvmhost.BasicJvmScriptingHost
import kotlin.script.experimental.jvmhost.createJvmCompilationConfigurationFromTemplate

class ConfisHost {
    private val compilationConfiguration = createJvmCompilationConfigurationFromTemplate<ConfisScriptDefinition>() {
        jvm {
            dependenciesFromCurrentContext(wholeClasspath = true)
            //libraries = arrayOf(PathUtil.getResourcePathForClass(Agreement::class.java).path),
        }
        ide { acceptedLocations(Everywhere) }
    }
    private val defaultHost = BasicJvmScriptingHost()

    fun eval(script: SourceCode): ResultWithDiagnostics<Agreement> {
        //val res = replHost.compile(script, compilationConfiguration)

        val res = defaultHost.eval(script, compilationConfiguration, null)
        if (res !is Success) {
            res.reports.forEach {
                logger<ConfisHost>().warn(it.message, it.exception)
            }
        }

        val ps = Thread.currentThread().contextClassLoader.definedPackages.joinToString(separator = "\n")
        logger<ConfisHost>().warn("TOLO: $ps")

        return res.map {
            val builder = (it.returnValue.scriptInstance as AgreementBuilder)
            AgreementBuilder.assemble(builder)
        }
    }
}
