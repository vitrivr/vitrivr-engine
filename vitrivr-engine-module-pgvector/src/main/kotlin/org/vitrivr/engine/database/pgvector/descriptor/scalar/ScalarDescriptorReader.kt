package org.vitrivr.engine.database.pgvector.descriptor.scalar

import org.vitrivr.engine.core.model.descriptor.scalar.*
import org.vitrivr.engine.core.model.descriptor.scalar.ScalarDescriptor.Companion.VALUE_ATTRIBUTE_NAME
import org.vitrivr.engine.core.model.descriptor.struct.StructDescriptor
import org.vitrivr.engine.core.model.descriptor.vector.*
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.query.Query
import org.vitrivr.engine.core.model.retrievable.Retrieved
import org.vitrivr.engine.core.model.types.Value
import org.vitrivr.engine.database.pgvector.DESCRIPTOR_ID_COLUMN_NAME
import org.vitrivr.engine.database.pgvector.PgVectorConnection
import org.vitrivr.engine.database.pgvector.RETRIEVABLE_ID_COLUMN_NAME
import org.vitrivr.engine.database.pgvector.descriptor.AbstractDescriptorReader
import java.sql.ResultSet
import java.util.*

/**
 * A [DescriptorReader] for [ScalarDescriptor]s.
 *
 * @author Ralph Gasser
 * @version 1.1.0
 */
class ScalarDescriptorReader(field: Schema.Field<*, ScalarDescriptor<*>>, connection: PgVectorConnection) : AbstractDescriptorReader<ScalarDescriptor<*>>(field, connection) {

    /**
     * Executes the provided [Query] and returns a [Sequence] of [Retrieved]s that match it.
     *
     * @param query The [Query] to execute.
     * @return [Sequence] of [StructDescriptor]s that match the query.
     */
    override fun query(query: Query): Sequence<ScalarDescriptor<*>> {
        TODO("Not yet implemented")
    }

    /**
     * Converts the provided [ResultSet] to a [VectorDescriptor].
     *
     * @param result The [ResultSet] to convert.
     * @return The resulting [VectorDescriptor].
     */
    override fun rowToDescriptor(result: ResultSet): ScalarDescriptor<*> {
        val descriptorId = result.getObject(DESCRIPTOR_ID_COLUMN_NAME, UUID::class.java)
        val retrievableId = result.getObject(RETRIEVABLE_ID_COLUMN_NAME, UUID::class.java)
        return when (this.prototype) {
            is BooleanDescriptor -> BooleanDescriptor(descriptorId, retrievableId, Value.Boolean(result.getBoolean(VALUE_ATTRIBUTE_NAME)))
            is IntDescriptor -> IntDescriptor(descriptorId, retrievableId, Value.Int(result.getInt(VALUE_ATTRIBUTE_NAME)))
            is LongDescriptor -> LongDescriptor(descriptorId, retrievableId, Value.Long(result.getLong(VALUE_ATTRIBUTE_NAME)))
            is FloatDescriptor -> FloatDescriptor(descriptorId, retrievableId, Value.Float(result.getFloat(VALUE_ATTRIBUTE_NAME)))
            is DoubleDescriptor -> DoubleDescriptor(descriptorId, retrievableId, Value.Double(result.getDouble(VALUE_ATTRIBUTE_NAME)))
            is StringDescriptor -> StringDescriptor(descriptorId, retrievableId, Value.String(result.getString(VALUE_ATTRIBUTE_NAME)))
        }
    }
}