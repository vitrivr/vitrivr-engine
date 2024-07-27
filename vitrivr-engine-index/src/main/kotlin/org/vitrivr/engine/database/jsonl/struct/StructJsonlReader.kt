package org.vitrivr.engine.database.jsonl.struct

import org.vitrivr.engine.core.model.descriptor.AttributeName
import org.vitrivr.engine.core.model.descriptor.struct.StructDescriptor
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.types.Value
import org.vitrivr.engine.database.jsonl.AbstractJsonlReader
import org.vitrivr.engine.database.jsonl.JsonlConnection
import org.vitrivr.engine.database.jsonl.JsonlConnection.Companion.DESCRIPTOR_ID_COLUMN_NAME
import org.vitrivr.engine.database.jsonl.JsonlConnection.Companion.RETRIEVABLE_ID_COLUMN_NAME
import org.vitrivr.engine.database.jsonl.model.AttributeContainerList
import kotlin.reflect.full.primaryConstructor

class StructJsonlReader (
    field: Schema.Field<*, StructDescriptor>,
    connection: JsonlConnection
) : AbstractJsonlReader<StructDescriptor>(field, connection) {

    override fun toDescriptor(list: AttributeContainerList): StructDescriptor {

        val map = list.list.associateBy { it.attribute.name }
        val constructor = this.field.analyser.descriptorClass.primaryConstructor ?: throw IllegalStateException("Provided type ${this.field.analyser.descriptorClass} does not have a primary constructor.")
        val valueMap = mutableMapOf<AttributeName, Value<*>>()

        val retrievableId = (map[DESCRIPTOR_ID_COLUMN_NAME]?.value!!.toValue() as Value.UUIDValue).value
        val descriptorId = (map[RETRIEVABLE_ID_COLUMN_NAME]?.value!!.toValue() as Value.UUIDValue).value
        val parameters: MutableList<Any?> = mutableListOf(
            descriptorId,
            retrievableId,
            valueMap
        )

        prototype.layout().forEach { attribute ->
            val value = map[attribute.name]!!.value?.toValue()!!
            valueMap[attribute.name] = value
        }

        return constructor.call(*parameters.toTypedArray())
    }
}