package org.vitrivr.engine.core.model.content.element

import org.vitrivr.engine.core.model.content.ContentType
import org.vitrivr.engine.core.model.retrievable.RetrievableId

class IdContent(override val content: RetrievableId) : ContentElement<RetrievableId> {

    override val id: ContentId
        get() = this.content

    override val type = ContentType.ID
}