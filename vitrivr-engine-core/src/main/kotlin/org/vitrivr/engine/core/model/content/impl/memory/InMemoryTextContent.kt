package org.vitrivr.engine.core.model.content.impl.memory

import kotlinx.serialization.Serializable
import org.vitrivr.engine.core.model.content.element.ContentId
import org.vitrivr.engine.core.model.content.element.TextContent
import java.util.UUID

/**
 * A naive in-memory implementation of the [TextContent] interface.
 *
 * @author Luca Rossetto.
 * @version 1.0.0
 */

data class InMemoryTextContent(override val content: String, override val id: ContentId = ContentId.randomUUID()) : TextContent