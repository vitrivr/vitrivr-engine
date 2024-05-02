package org.vitrivr.engine.core.util.extension

import org.vitrivr.engine.core.model.content.element.ImageContent
import org.vitrivr.engine.core.model.content.element.TextContent
import org.vitrivr.engine.core.source.file.MimeType
import java.nio.charset.StandardCharsets
import java.util.*

/**
 * Converts this [ImageContent] to a data URL representation.
 *
 * @param mimeType [MimeType] to use. Defaults to [MimeType.PNG]
 * @return [String] of the data URL.
 */
fun ImageContent.toDataUrl(mimeType: MimeType = MimeType.PNG): String = this.content.toDataURL(mimeType)

/**
 * Converts this [TextContent] to a data URL representation.
 *
 * @return [String] of the data URL.
 */
fun TextContent.toDataUrl(): String = "data:text/plain;charset=utf-8,${Base64.getEncoder().encodeToString(this.content.toByteArray(StandardCharsets.UTF_8))}"