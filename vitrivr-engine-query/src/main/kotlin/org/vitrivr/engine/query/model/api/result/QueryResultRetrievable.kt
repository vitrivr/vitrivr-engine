package org.vitrivr.engine.query.model.api.result

import kotlinx.serialization.Serializable
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.model.retrievable.attributes.PropertyAttribute
import org.vitrivr.engine.core.model.retrievable.attributes.ScoreAttribute
import org.vitrivr.engine.core.model.types.Value
import org.vitrivr.engine.query.model.api.input.InputDataSerializer
import javax.swing.text.html.HTMLEditorKit

typealias RetrievableIdString = String

@Serializable
data class QueryResultRetrievable(
    val id: RetrievableIdString,
    val score: Double,
    val type: String,
    val relationship: MutableMap<String, QueryResultRetrievable>,
    val properties: Map<String, String>,
    val descriptors: Map<String, Value<*>>
) {
    constructor(retrieved: Retrievable) : this(
        retrieved.id.toString(),
        retrieved.filteredAttribute(ScoreAttribute::class.java)?.score ?: 0.0,
        retrieved.type ?: "",
        mutableMapOf(),
        retrieved.filteredAttributes(PropertyAttribute::class.java).firstOrNull()?.properties ?: emptyMap(),
        retrieved.descriptors.flatMap { descriptor ->
            descriptor.values().map {
                descriptor.field?.fieldName + "." + it.key to it.value!!
            }
        }.toMap()
    )

}