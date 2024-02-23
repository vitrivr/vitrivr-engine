package org.vitrivr.engine.core.model.retrievable.attributes

import org.vitrivr.engine.core.model.retrievable.Relationship

class RelationshipAttribute(relationships: Collection<Relationship>) : MergingRetrievableAttribute {

    val relationships = relationships.toSet()
    override fun merge(other: MergingRetrievableAttribute): RelationshipAttribute = RelationshipAttribute(this.relationships + (other as RelationshipAttribute).relationships)
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as RelationshipAttribute

        return relationships == other.relationships
    }

    override fun hashCode(): Int {
        return relationships.hashCode()
    }


}