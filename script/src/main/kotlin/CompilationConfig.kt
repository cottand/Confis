package eu.dcotta.confis.scripting

import eu.dcotta.confis.model.Purpose
import kotlin.script.experimental.api.ScriptAcceptedLocation.Everywhere
import kotlin.script.experimental.api.ScriptCompilationConfiguration
import kotlin.script.experimental.api.ScriptEvaluationConfiguration
import kotlin.script.experimental.api.acceptedLocations
import kotlin.script.experimental.api.defaultImports
import kotlin.script.experimental.api.ide
import kotlin.script.experimental.api.scriptsInstancesSharing
import kotlin.script.experimental.jvm.dependenciesFromCurrentContext
import kotlin.script.experimental.jvm.jvm
import java.io.File

object CompilationConfig : ScriptCompilationConfiguration({
    defaultImports(File::class, Purpose.Commercial::class)

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
    scriptsInstancesSharing(true) // if a script is imported multiple times in the import hierarchy, use a single copy
})
