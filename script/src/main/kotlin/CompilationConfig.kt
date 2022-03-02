package eu.dcotta.confis.scripting

import kotlin.script.experimental.api.ScriptCompilationConfiguration
import kotlin.script.experimental.api.defaultImports
import kotlin.script.experimental.api.refineConfiguration
import kotlin.script.experimental.jvm.dependenciesFromCurrentContext
import kotlin.script.experimental.jvm.jvm
import java.io.File

object CompilationConfig: ScriptCompilationConfiguration( {
        defaultImports(File::class)

        jvm {
            // the dependenciesFromCurrentContext helper function extracts the classpath from current thread classloader
            // and take jars with mentioned names to the compilation classpath via `dependencies` key.
            // to add the whole classpath for the classloader without check for jar presense, use
            // `dependenciesFromCurrentContext(wholeClasspath = true)`
            dependenciesFromCurrentContext(
                "script", // script library jar name
            )
        }
})

