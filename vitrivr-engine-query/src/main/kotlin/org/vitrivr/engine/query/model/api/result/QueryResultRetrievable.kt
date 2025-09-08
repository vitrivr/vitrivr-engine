package org.vitrivr.engine.query.model.api.result

import kotlinx.serialization.Serializable
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.model.retrievable.attributes.PropertyAttribute
import org.vitrivr.engine.core.model.retrievable.attributes.ScoreAttribute

typealias RetrievableIdString = String

@Serializable
data class QueryResultRetrievable(
    val id: RetrievableIdString,
    val score: Double,
    val type: String,
    val relationship: MutableMap<String, QueryResultRetrievable>,
    val properties: Map<String, String>,
    val descriptors: Map<String, String>
) {
    constructor(retrieved: Retrievable) : this(
        retrieved.id.toString(),
        retrieved.filteredAttribute(ScoreAttribute::class.java)?.score ?: 0.0,
        retrieved.type ?: "",
        mutableMapOf(),
        retrieved.filteredAttributes(PropertyAttribute::class.java).firstOrNull()?.properties ?: emptyMap(),
        retrieved.descriptors.flatMap { descriptor ->
            descriptor.values().map { descriptor.field?.fieldName + "." + it.key to it.value?.value.toString() }
        }.toMap()
    )
}