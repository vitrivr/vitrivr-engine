package org.vitrivr.engine.core.model.descriptor.struct.metadata.source

import org.vitrivr.engine.core.model.descriptor.DescriptorId
import org.vitrivr.engine.core.model.descriptor.FieldSchema
import org.vitrivr.engine.core.model.types.Type
import org.vitrivr.engine.core.model.descriptor.struct.StructDescriptor
import org.vitrivr.engine.core.model.retrievable.RetrievableId
import java.util.*
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
                "STRING" -> FieldSchema(key, Type.STRING)
                "BOOLEAN" -> FieldSchema(key, Type.BOOLEAN)
                "BYTE" -> FieldSchema(key, Type.BYTE)
                "SHORT" -> FieldSchema(key, Type.SHORT)
                "INT" -> FieldSchema(key, Type.INT)
                "LONG" -> FieldSchema(key, Type.LONG)
                "FLOAT" -> FieldSchema(key, Type.FLOAT)
                "DOUBLE" -> FieldSchema(key, Type.DOUBLE)
                else -> FieldSchema(key, Type.STRING)
            }
        }
    }

    override fun values(): List<Pair<String, Any?>> {
        return this.metadata.map { (key, _) ->
            key to this::class.memberProperties.find { it.name == key }?.getter?.call(this)
        }
    }
}