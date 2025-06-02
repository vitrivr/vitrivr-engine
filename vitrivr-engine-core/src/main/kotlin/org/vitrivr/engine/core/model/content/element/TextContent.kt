package org.vitrivr.engine.core.model.content.element

import org.vitrivr.engine.core.model.content.ContentType
import org.vitrivr.engine.core.model.descriptor.scalar.TextDescriptor
import org.vitrivr.engine.core.model.types.Value
import java.nio.charset.StandardCharsets
import java.util.*

/**
 * A textual [ContentElement].
 *
 * @author Ralph Gasser
 * @author Luca Rossetto
 * @version 1.1.0
 */
interface TextContent: DescriptorContent<TextDescriptor> {
    /** Length of the [String] held by this [TextContent]. */
    val length: Int
        get() = (this.content as TextDescriptor).value.value.length

    val text: String
        get() = (this.content as TextDescriptor).value.value

    /** The [ContentType] of an [TextContent] is always [ContentType.TEXT]. */
    override val type: ContentType
        get() = ContentType.DESCRIPTOR

    /**
     * Converts this [TextContent] to a data URL representation.
     *
     * @return [String] of the data URL.
     */
    fun toDataUrl(): String = "data:text/plain;charset=utf-8,${Base64.getEncoder().encodeToString((this.content as Value.Text).value.toByteArray(StandardCharsets.UTF_8))}"
}