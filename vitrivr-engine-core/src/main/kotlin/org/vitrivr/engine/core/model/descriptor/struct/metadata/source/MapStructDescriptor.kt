package org.vitrivr.engine.core.model.descriptor.struct.metadata.source

import org.vitrivr.engine.core.model.descriptor.DescriptorId
import org.vitrivr.engine.core.model.descriptor.FieldSchema
import org.vitrivr.engine.core.model.descriptor.struct.StructDescriptor
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.retrievable.RetrievableId
import org.vitrivr.engine.core.model.types.Type
import org.vitrivr.engine.core.model.types.Value
import java.util.*

data class MapStructDescriptor(
    override val id: DescriptorId,
    override val retrievableId: RetrievableId,
    val columnTypes: Map<String, String>,
    val columnValues: Map<String, Any?>,
    override val transient: Boolean = false,
    override val field: Schema.Field<*, MapStructDescriptor>? = null
) : StructDescriptor {

    companion object{
        fun prototype(columnTypes: Map<String, String>): MapStructDescriptor {
            val columnValues = columnTypes.mapValues { (_, type) ->
                when (Type.valueOf(type)) {
                    Type.STRING -> ""
                    Type.BOOLEAN -> false
                    Type.BYTE -> 0.toByte()
                    Type.SHORT -> 0.toShort()
                    Type.INT -> 0
                    Type.LONG -> 0L
                    Type.FLOAT -> 0.0f
                    Type.DOUBLE -> 0.0
                    Type.DATETIME -> Date()
                }
            }
            return MapStructDescriptor(UUID.randomUUID(), UUID.randomUUID(), columnTypes, columnValues)

        }
    }

    override fun schema(): List<FieldSchema> {
        return this.columnTypes.map { (key, type) ->
            FieldSchema(key, Type.valueOf(type), nullable=true)
        }
    }

    override fun values(): List<Pair<String, Any?>> {
        return this.columnTypes.map { (key, type) ->
            val value = this.columnValues[key] // This will be null if key is not present in columnValues
            val pairedValue = when (Type.valueOf(type)) {
                Type.STRING -> (value as? Value.String)
                Type.BOOLEAN -> (value as? Value.Boolean)
                Type.BYTE -> (value as? Value.Byte)
                Type.SHORT -> (value as? Value.Short)
                Type.INT -> (value as? Value.Int)
                Type.LONG -> (value as? Value.Long)
                Type.FLOAT -> (value as? Value.Float)
                Type.DOUBLE -> (value as? Value.Double)
                Type.DATETIME -> (value as? Value.DateTime)
            }
            Pair(key, pairedValue)
        }

    }
}
