package eu.dcotta.confis.scripting.eu.dcotta.confis.scripting

import kotlin.script.experimental.api.SourceCode

data class ConfisSourceCode(
    override val locationId: String?,
    override val name: String?,
    override val text: String,
) : SourceCode
