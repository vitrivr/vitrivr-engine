package org.vitrivr.engine.core.model.content.impl

import kotlinx.serialization.Serializable
import org.vitrivr.engine.core.model.content.TextContent

@Serializable
@JvmRecord
internal data class InMemoryTextContent(private val text: String) : TextContent {
    override fun getContent(): String = this.text
}
