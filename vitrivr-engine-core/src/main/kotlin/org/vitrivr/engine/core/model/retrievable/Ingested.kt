package org.vitrivr.engine.core.model.retrievable

import org.vitrivr.engine.core.model.content.element.ContentElement
import org.vitrivr.engine.core.model.descriptor.Descriptor
import org.vitrivr.engine.core.model.relationship.Relationship
import org.vitrivr.engine.core.model.retrievable.attributes.RetrievableAttribute

/**
 * A [Ingested] used in the data ingest pipeline.
 *
 * @author Ralph Gasser
 * @author Luca Rossetto
 * @version 2.0.0
 */
class Ingested(id: RetrievableId, type: String, content: List<ContentElement<*>> = emptyList(), descriptors: Set<Descriptor<*>> = emptySet(), attributes: Set<RetrievableAttribute> = emptySet(), relationships: Set<Relationship> = emptySet(), transient: Boolean) : AbstractRetrievable(id, type, content, descriptors, attributes, relationships, transient) {
    /**
     * Creates and returns a copy of this [Retrieved] but replaces provided attributes.
     *
     * @param id [RetrievableId] of the new [Retrieved]. Null if existing [RetrievableId] should be re-used.
     * @param content [List] of [ContentElement]s of the new [Retrieved]. Null if existing [ContentElement]s should be re-used.
     * @param descriptors [Set] of [Descriptor]s of the new [Retrieved]. Null if existing [Descriptor]s should be re-used.
     * @param attributes [Set] of [RetrievableAttribute]s of the new [Retrieved]. Null if existing [RetrievableAttribute]s should be re-used.
     * @param relationships [Set] of [Relationship]s of the new [Retrieved]. Null if existing [Relationship]s should be re-used.
     *
     * @return Copy of this [Retrieved] with replaced attributes.
     */
    override fun copy(id: RetrievableId?, type: String?, content: List<ContentElement<*>>?, descriptors: Set<Descriptor<*>>?, attributes: Set<RetrievableAttribute>?, relationships: Set<Relationship>?, transient: Boolean?) = Ingested(
        id = id ?: this.id,
        type = type ?: this.type,
        content = content ?: this.content,
        descriptors = descriptors ?: this.descriptors,
        attributes = attributes ?: this.attributes,
        relationships = relationships ?: this.relationships,
        transient = transient ?: this.transient
    )
}