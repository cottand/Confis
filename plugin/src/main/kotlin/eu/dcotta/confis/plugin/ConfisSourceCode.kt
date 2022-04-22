package eu.dcotta.confis.plugin

import kotlin.script.experimental.api.SourceCode

data class ConfisSourceCode(
    override val locationId: String?,
    override val name: String?,
    override val text: String,
) : SourceCode
