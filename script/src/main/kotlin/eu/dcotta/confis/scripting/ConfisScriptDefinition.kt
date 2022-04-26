package eu.dcotta.confis.scripting

import eu.dcotta.confis.dsl.AgreementBuilder
import kotlin.script.experimental.annotations.KotlinScript

// The KotlinScript annotation marks a class that can serve as a reference to the script definition for
// `createJvmCompilationConfigurationFromTemplate` call as well as for the discovery mechanism
// The marked class also become the base class for defined script type (unless redefined in the configuration)
@KotlinScript(
    // file name extension by which this script type is recognized by mechanisms built into scripting compiler plugin
    // and IDE support, it is recommended to use double extension with the last one being "kts", so some non-specific
    // scripting support could be used, e.g. in IDE, if the specific support is not installed.
    displayName = "Confis Agreement",
    fileExtension = CONFIS_FILE_EXTENSION,
    compilationConfiguration = CompilationConfig::class,
    evaluationConfiguration = EvaluationConfig::class,
)
// the class is used as the script base class, therefore it should be open or abstract
open class ConfisScriptDefinition : AgreementBuilder()
