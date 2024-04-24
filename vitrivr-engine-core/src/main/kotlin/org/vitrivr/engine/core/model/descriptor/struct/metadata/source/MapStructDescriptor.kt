package org.vitrivr.engine.core.model.descriptor.struct.metadata.source

import org.vitrivr.engine.core.model.descriptor.DescriptorId
import org.vitrivr.engine.core.model.descriptor.FieldSchema
import org.vitrivr.engine.core.model.types.Type
import org.vitrivr.engine.core.model.descriptor.struct.StructDescriptor
import org.vitrivr.engine.core.model.retrievable.RetrievableId
import java.util.*
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.internal.impl.types.TypeCheckerState.SupertypesPolicy.None

data class MapStructDescriptor(
    override val id: DescriptorId,
    override val retrievableId: RetrievableId,
    val columnTypes: Map<String, String>,
    val columnValues: Map<String, Any?>,
    override val transient: Boolean = false
) : StructDescriptor {

    companion object{
        fun prototype(columnTypes: Map<String, String>): MapStructDescriptor {
            val columnValues = columnTypes.mapValues { (_, type) ->
                when (type) {
                    "STRING" -> ""
                    "BOOLEAN" -> false
                    "BYTE" -> 0.toByte()
                    "SHORT" -> 0.toShort()
                    "INT" -> 0
                    "LONG" -> 0L
                    "FLOAT" -> 0.0f
                    "DOUBLE" -> 0.0
                    "DATETIME" -> Date()
                    else -> throw(IllegalArgumentException("Unsupported type $type"))
                }
            }
            return MapStructDescriptor(UUID.randomUUID(), UUID.randomUUID(), columnTypes, columnValues)

        }
    }

    override fun schema(): List<FieldSchema> {
        return this.columnTypes.map { (key, value) ->
            when (value) {
                "STRING" -> FieldSchema(key, Type.STRING, nullable=true)
                "BOOLEAN" -> FieldSchema(key, Type.BOOLEAN, nullable=true)
                "BYTE" -> FieldSchema(key, Type.BYTE, nullable=true)
                "SHORT" -> FieldSchema(key, Type.SHORT, nullable=true)
                "INT" -> FieldSchema(key, Type.INT, nullable=true)
                "LONG" -> FieldSchema(key, Type.LONG, nullable=true)
                "FLOAT" -> FieldSchema(key, Type.FLOAT, nullable=true)
                "DOUBLE" -> FieldSchema(key, Type.DOUBLE, nullable=true)
                "DATETIME" -> FieldSchema(key, Type.DATETIME, nullable=true)
                else -> throw(IllegalArgumentException("Unsupported type $value"))
            }
        }
    }

    override fun values(): List<Pair<String, Any?>> {
        return this.columnTypes.map { (key, type) ->
            val value = this.columnValues[key] // This will be null if key is not present in columnValues
            val pairedValue = when (type) {
                "STRING" -> value as? String
                "BOOLEAN" -> value as? Boolean
                "BYTE" -> value as? Byte
                "SHORT" -> value as? Short
                "INT" -> value as? Int
                "LONG" -> value as? Long
                "FLOAT" -> value as? Float
                "DOUBLE" -> value as? Double
                "DATETIME" -> value as? Date
                else -> throw IllegalArgumentException("Unsupported type $type")
            }
            Pair(key, pairedValue)
        }

    }
}