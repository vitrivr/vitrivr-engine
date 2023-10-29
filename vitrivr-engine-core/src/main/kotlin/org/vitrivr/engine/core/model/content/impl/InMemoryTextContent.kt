package org.vitrivr.engine.core.model.content.impl

import kotlinx.serialization.Serializable
import org.vitrivr.engine.core.model.content.element.TextContent

/**
 * A naive in-memory implementation of the [TextContent] interface.
 *
 * @author Luca Rossetto.
 * @version 1.0.0
 */
@Serializable
data class InMemoryTextContent(override val content: String) : TextContent
