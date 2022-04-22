package eu.dcotta.confis.plugin

import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.diagnostic.thisLogger
import eu.dcotta.confis.dsl.AgreementBuilder
import eu.dcotta.confis.model.Agreement
import eu.dcotta.confis.scripting.ConfisScriptDefinition
import kotlin.script.experimental.api.ResultWithDiagnostics
import kotlin.script.experimental.api.ResultWithDiagnostics.Success
import kotlin.script.experimental.api.ScriptAcceptedLocation.Everywhere
import kotlin.script.experimental.api.SourceCode
import kotlin.script.experimental.api.acceptedLocations
import kotlin.script.experimental.api.baseClass
import kotlin.script.experimental.api.dependencies
import kotlin.script.experimental.api.ide
import kotlin.script.experimental.host.ScriptingHostConfiguration
import kotlin.script.experimental.jvm.baseClassLoader
import kotlin.script.experimental.jvm.dependenciesFromClassloader
import kotlin.script.experimental.jvm.jvm
import kotlin.script.experimental.jvmhost.BasicJvmScriptingHost
import kotlin.script.experimental.jvmhost.createJvmCompilationConfigurationFromTemplate

class ConfisHost {

    private val compilationConfiguration = createJvmCompilationConfigurationFromTemplate<ConfisScriptDefinition> {
        jvm {
            dependenciesFromClassloader(classLoader = ConfisHost::class.java.classLoader, wholeClasspath = true)
            // updateClasspath(getJarForClass(
            //    AgreementBuilder::class,
            //    ConfisScriptDefinition::class,
            //    PersistentList::class,
            // ))
            val deps = this[dependencies] ?: emptyList()
            logger<ConfisHost>().warn("Classpath contains following confis packages: $deps")
        }
        baseClass(ConfisScriptDefinition::class)
        ide { acceptedLocations(Everywhere) }
    }
    private val defaultHost = BasicJvmScriptingHost(
        ScriptingHostConfiguration {
            jvm {
                baseClassLoader.put(ConfisHost::class.java.classLoader)
            }
        }
    )

    fun eval(script: SourceCode): ResultWithDiagnostics<Agreement> {
        // val res = replHost.compile(script, compilationConfiguration)
        thisLogger().debug("class ${ConfisScriptDefinition::class.qualifiedName}")
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
            val builder = (scriptInstance as ConfisScriptDefinition)
            AgreementBuilder.assemble(builder)
        }
    }
}
