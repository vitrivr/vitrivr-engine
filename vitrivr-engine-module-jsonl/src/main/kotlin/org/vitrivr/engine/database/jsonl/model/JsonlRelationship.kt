package org.vitrivr.engine.database.jsonl.model

import kotlinx.serialization.Serializable
import org.vitrivr.engine.core.model.relationship.Relationship
import org.vitrivr.engine.core.model.retrievable.RetrievableId

/**
 * Data class that represents a relationship in JSONL format.
 *
 * @author Luca Rossetto
 * @version 1.0.0
 */
@Serializable
data class JsonlRelationship(val sub: RetrievableId, val pred: String, val obj: RetrievableId) {
    constructor(relationship: Relationship) : this(relationship.subjectId, relationship.predicate, relationship.objectId)
    fun toRelationship() = Relationship.ById(this.sub, this.pred, this.obj, false)
}
