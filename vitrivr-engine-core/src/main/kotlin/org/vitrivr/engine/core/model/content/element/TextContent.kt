package org.vitrivr.engine.core.model.content.element

import org.vitrivr.engine.core.model.content.ContentType
import java.nio.charset.StandardCharsets
import java.util.*

/**
 * A textual [ContentElement].
 *
 * @author Ralph Gasser
 * @author Luca Rossetto
 * @version 1.1.0
 */
interface TextContent: ContentElement<String> {
    /** Length of the [String] held by this [TextContent]. */
    val length: Int
        get() = this.content.length

    /** The [ContentType] of an [TextContent] is always [ContentType.TEXT]. */
    override val type: ContentType
        get() = ContentType.TEXT

    /**
     * Converts this [TextContent] to a data URL representation.
     *
     * @return [String] of the data URL.
     */
    fun toDataUrl(): String = "data:text/plain;charset=utf-8,${Base64.getEncoder().encodeToString(this.content.toByteArray(StandardCharsets.UTF_8))}"
}