package eu.dcotta.confis.plugin

import eu.dcotta.confis.scripting.hybridCacheConfiguration
import kotlin.script.experimental.host.ScriptingHostConfiguration
import kotlin.script.experimental.jvm.baseClassLoader
import kotlin.script.experimental.jvm.jvm
import kotlin.script.experimental.jvmhost.BasicJvmScriptingHost

object Resources {

    val jvmScriptingHost by lazy {
        BasicJvmScriptingHost(
            ScriptingHostConfiguration {
                jvm {
                    baseClassLoader.put(ConfisHost::class.java.classLoader)
                    // compilationCache(InMemoryCache(maxSize = 40))
                    hybridCacheConfiguration()
                }
            }
        )
    }

    val scriptHost by lazy {
        ConfisHost(jvmScriptingHost)
    }
}
