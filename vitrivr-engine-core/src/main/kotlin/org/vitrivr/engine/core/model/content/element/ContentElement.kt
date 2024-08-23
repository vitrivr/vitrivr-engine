package org.vitrivr.engine.core.model.content.element

import kotlinx.serialization.Serializable
import org.vitrivr.engine.core.model.content.Content
import org.vitrivr.engine.core.model.content.ContentType
import org.vitrivr.engine.core.model.serializer.UUIDSerializer
import java.util.UUID

typealias ContentId = @Serializable(UUIDSerializer::class) UUID

/**
 * A [Content] element is a piece of [Content] that is tied to some actual [Content].
 *
 * The types of [ContentElement] are restricted
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
sealed interface ContentElement<T>: Content {
    /**
     * Accesses the content held by  this [ContentElement].
     *
     * @return [ContentElement]
     */
    val content: T

    val id: ContentId

    /** The [ContentType] of this [ContentElement]. */
    val type: ContentType

}