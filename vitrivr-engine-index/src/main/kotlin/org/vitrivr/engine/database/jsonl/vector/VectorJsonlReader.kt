package org.vitrivr.engine.database.jsonl.vector

import org.vitrivr.engine.core.model.descriptor.vector.*
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.types.Value
import org.vitrivr.engine.database.jsonl.AbstractJsonlReader
import org.vitrivr.engine.database.jsonl.model.AttributeContainerList
import org.vitrivr.engine.database.jsonl.JsonlConnection
import org.vitrivr.engine.database.jsonl.JsonlConnection.Companion.DESCRIPTOR_ID_COLUMN_NAME
import org.vitrivr.engine.database.jsonl.JsonlConnection.Companion.RETRIEVABLE_ID_COLUMN_NAME

class VectorJsonlReader(
    field: Schema.Field<*, VectorDescriptor<*>>,
    connection: JsonlConnection
) : AbstractJsonlReader<VectorDescriptor<*>>(field, connection) {

    override fun toDescriptor(list: AttributeContainerList): VectorDescriptor<*> {

        val map = list.list.associateBy { it.attribute.name }
        val retrievableId = (map[DESCRIPTOR_ID_COLUMN_NAME]?.value!!.toValue() as Value.UUIDValue).value
        val descriptorId = (map[RETRIEVABLE_ID_COLUMN_NAME]?.value!!.toValue() as Value.UUIDValue).value
        val value = map["vector"]?.value!!.toValue()

        return when (prototype) {
            is BooleanVectorDescriptor -> BooleanVectorDescriptor(
                descriptorId,
                retrievableId,
                value as Value.BooleanVector
            )

            is FloatVectorDescriptor -> FloatVectorDescriptor(
                descriptorId,
                retrievableId,
                value as Value.FloatVector
            )

            is DoubleVectorDescriptor -> DoubleVectorDescriptor(
                descriptorId,
                retrievableId,
                value as Value.DoubleVector
            )

            is IntVectorDescriptor -> IntVectorDescriptor(
                descriptorId,
                retrievableId,
                value as Value.IntVector
            )

            is LongVectorDescriptor -> LongVectorDescriptor(
                descriptorId,
                retrievableId,
                value as Value.LongVector
            )
        }
    }
}