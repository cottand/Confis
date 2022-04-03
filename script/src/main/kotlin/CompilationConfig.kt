package eu.dcotta.confis.scripting

import eu.dcotta.confis.dsl.AgreementBuilder
import eu.dcotta.confis.model.Purpose
import kotlin.script.experimental.api.ScriptAcceptedLocation.Everywhere
import kotlin.script.experimental.api.ScriptCompilationConfiguration
import kotlin.script.experimental.api.ScriptEvaluationConfiguration
import kotlin.script.experimental.api.acceptedLocations
import kotlin.script.experimental.api.baseClass
import kotlin.script.experimental.api.defaultIdentifier
import kotlin.script.experimental.api.defaultImports
import kotlin.script.experimental.api.displayName
import kotlin.script.experimental.api.fileExtension
import kotlin.script.experimental.api.filePathPattern
import kotlin.script.experimental.api.hostConfiguration
import kotlin.script.experimental.api.ide
import kotlin.script.experimental.api.implicitReceivers
import kotlin.script.experimental.api.scriptsInstancesSharing
import kotlin.script.experimental.jvm.dependenciesFromCurrentContext
import kotlin.script.experimental.jvm.impl.scriptMetadataPath
import kotlin.script.experimental.jvm.jvm

object CompilationConfig : ScriptCompilationConfiguration({
    defaultImports(
        Purpose.Commercial::class,
    )

    defaultImports(
        Purpose::class.qualifiedName + ".*",
        AgreementBuilder::class.java.packageName + ".*",
        "eu.dcotta.confis.dsl.*",
        "import eu.dcotta.confis.model.Month.*"
    )

    baseClass(AgreementBuilder::class)
    fileExtension("confis.kts")
    displayName("Confis Agreement")

    defaultIdentifier("ConfisScript")
    println("Using default imports:\n  " + this[defaultImports]?.joinToString("\n  "))

    jvm {
        // the dependenciesFromCurrentContext helper function extracts the classpath from current thread classloader
        // and take jars with mentioned names to the compilation classpath via `dependencies` key.
        // to add the whole classpath for the classloader without check for jar presense, use
        // `dependenciesFromCurrentContext(wholeClasspath = true)`
        //dependenciesFromCurrentContext(wholeClasspath = true)
        dependenciesFromCurrentContext("script", "lib")
    }

    ide {
        acceptedLocations(Everywhere)
    }
})

object EvaluationConfig : ScriptEvaluationConfiguration({
    scriptsInstancesSharing(false) // if a script is imported multiple times in the import hierarchy, use a single copy
})
