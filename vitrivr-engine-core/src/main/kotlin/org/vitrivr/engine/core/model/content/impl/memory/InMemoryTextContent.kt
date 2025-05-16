package org.vitrivr.engine.core.model.content.impl.memory

import kotlinx.serialization.Serializable
import org.vitrivr.engine.core.model.content.element.ContentId
import org.vitrivr.engine.core.model.content.element.DescriptorContent
import org.vitrivr.engine.core.model.content.element.TextContent
import org.vitrivr.engine.core.model.descriptor.scalar.TextDescriptor
import org.vitrivr.engine.core.model.types.Value
import java.util.UUID

/**
 * A naive in-memory implementation of the [TextContent] interface.
 *
 * @author Luca Rossetto.
 * @version 1.0.0
 */

data class InMemoryTextContent(override val content: TextDescriptor, override val id: ContentId = ContentId.randomUUID()) : DescriptorContent<TextDescriptor> {
    constructor(text: String, id: ContentId = ContentId.randomUUID()) : this(TextDescriptor(id, null, Value.Text(text), null), id)
}