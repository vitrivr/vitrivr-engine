package org.vitrivr.engine.core.model.retrievable.attributes

import org.vitrivr.engine.core.model.content.element.ContentElement

data class ContentAttribute(val content: ContentElement<*>) : RetrievableAttribute {
    val type = content.type
}