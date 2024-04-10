package org.vitrivr.engine.core.model.descriptor.struct.metadata.source

import org.vitrivr.engine.core.model.descriptor.DescriptorId
import org.vitrivr.engine.core.model.descriptor.FieldSchema
import org.vitrivr.engine.core.model.descriptor.FieldType
import org.vitrivr.engine.core.model.descriptor.struct.StructDescriptor
import org.vitrivr.engine.core.model.retrievable.RetrievableId
import kotlin.reflect.full.memberProperties

data class MapStructDescriptor(
    override val id: DescriptorId,
    override val retrievableId: RetrievableId,
    val metadata: Map<String, String>,
    override val transient: Boolean = false
) : StructDescriptor {


    override fun schema(): List<FieldSchema> {
        return this.metadata.map { (key, value) ->
            when (value) {
                "STRING" -> FieldSchema(key, FieldType.STRING)
                "BOOLEAN" -> FieldSchema(key, FieldType.BOOLEAN)
                "BYTE" -> FieldSchema(key, FieldType.BYTE)
                "SHORT" -> FieldSchema(key, FieldType.SHORT)
                "INT" -> FieldSchema(key, FieldType.INT)
                "LONG" -> FieldSchema(key, FieldType.LONG)
                "FLOAT" -> FieldSchema(key, FieldType.FLOAT)
                "DOUBLE" -> FieldSchema(key, FieldType.DOUBLE)
                else -> FieldSchema(key, FieldType.STRING)
            }
        }
    }

    override fun values(): List<Pair<String, Any?>> {
        /*return this.metadata.map { (key, _) ->
            key to this::class.memberProperties.find { it.name == key }?.getter?.call(this)
        }*/
        TODO("Not yet implemented")
    }
}