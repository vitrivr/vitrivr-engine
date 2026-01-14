package org.vitrivr.engine.query.model.api.result

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonPrimitive
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.model.retrievable.attributes.PropertyAttribute
import org.vitrivr.engine.core.model.retrievable.attributes.ScoreAttribute
import org.vitrivr.engine.core.model.types.DescriptorMapContentOnlySerializer
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
    @Serializable(with = DescriptorMapContentOnlySerializer::class)
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
                descriptor.field?.fieldName + "." + it.key to  toValue(it.value!!)
            }
        }.toMap()
    )

    companion object {

        private fun toValue(v: Any): org.vitrivr.engine.core.model.types.Value<*> =
            when (v) {
                is org.vitrivr.engine.core.model.types.Value<*> -> v

                is FloatArray -> org.vitrivr.engine.core.model.types.Value.FloatVector(v)
                is DoubleArray -> org.vitrivr.engine.core.model.types.Value.DoubleVector(v)
                is IntArray -> org.vitrivr.engine.core.model.types.Value.IntVector(v)
                is LongArray -> org.vitrivr.engine.core.model.types.Value.LongVector(v)
                is BooleanArray -> org.vitrivr.engine.core.model.types.Value.BooleanVector(v)

                is String -> org.vitrivr.engine.core.model.types.Value.String(v)
                is Boolean -> org.vitrivr.engine.core.model.types.Value.Boolean(v)
                is Byte -> org.vitrivr.engine.core.model.types.Value.Byte(v)
                is Short -> org.vitrivr.engine.core.model.types.Value.Short(v)
                is Int -> org.vitrivr.engine.core.model.types.Value.Int(v)
                is Long -> org.vitrivr.engine.core.model.types.Value.Long(v)
                is Float -> org.vitrivr.engine.core.model.types.Value.Float(v)
                is Double -> org.vitrivr.engine.core.model.types.Value.Double(v)

                is java.util.Date -> org.vitrivr.engine.core.model.types.Value.DateTime(v)
                is java.util.UUID -> org.vitrivr.engine.core.model.types.Value.UUIDValue(v)

                else -> throw IllegalArgumentException("Unsupported descriptor value type: ${v::class.qualifiedName}")
            }

    }

}
