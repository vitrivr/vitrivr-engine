package org.vitrivr.engine.database.jsonl.model

import kotlinx.serialization.Serializable
import org.vitrivr.engine.core.model.relationship.Relationship
import org.vitrivr.engine.core.model.retrievable.RetrievableId

@Serializable
data class JsonlRelationship(val sub: RetrievableId, val pred: String, val obj: RetrievableId) {
    constructor(relationship: Relationship) : this(relationship.subjectId, relationship.predicate, relationship.objectId)

    fun toTriple() : Triple<RetrievableId, String, RetrievableId> = Triple(sub, pred, obj)

}
