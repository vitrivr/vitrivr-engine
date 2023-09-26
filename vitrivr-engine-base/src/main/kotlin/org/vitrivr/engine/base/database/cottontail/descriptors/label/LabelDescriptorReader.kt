package org.vitrivr.engine.base.database.cottontail.descriptors.label

import org.vitrivr.cottontail.core.tuple.Tuple
import org.vitrivr.engine.base.database.cottontail.CottontailConnection
import org.vitrivr.engine.base.database.cottontail.descriptors.AbstractDescriptorReader
import org.vitrivr.engine.base.database.cottontail.descriptors.DESCRIPTOR_COLUMN_NAME
import org.vitrivr.engine.core.model.database.descriptor.struct.LabelDescriptor
import org.vitrivr.engine.core.model.database.retrievable.Retrieved
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.query.Query
import java.util.*

/**
 * An [AbstractDescriptorReader] for [LabelDescriptor]s.
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
class LabelDescriptorReader(field: Schema.Field<*, LabelDescriptor>, connection: CottontailConnection) : AbstractDescriptorReader<LabelDescriptor>(field, connection) {
    /**
     * Executes the provided [Query] and returns a [Sequence] of [Retrieved.WithDescriptor]s that match it.
     *
     * @param query The [Query] to execute.
     */
    override fun getAll(query: Query<LabelDescriptor>): Sequence<Retrieved> = throw UnsupportedOperationException("Query of typ ${query::class} is not supported by FloatVectorDescriptorReader.")

    /**
     * Converts the provided [Tuple] to a [LabelDescriptor].
     *
     * @param tuple The [Tuple] to convert.
     * @return The resulting [LabelDescriptor].
     */
    override fun tupleToDescriptor(tuple: Tuple): LabelDescriptor = LabelDescriptor(
        UUID.fromString(tuple.asString(CottontailConnection.RETRIEVABLE_ID_COLUMN_NAME) ?: throw IllegalArgumentException("The provided tuple is missing the required field '${CottontailConnection.RETRIEVABLE_ID_COLUMN_NAME}'.")),
        UUID.fromString(tuple.asString(CottontailConnection.DESCRIPTOR_ID_COLUMN_NAME) ?: throw IllegalArgumentException("The provided tuple is missing the required field '${CottontailConnection.DESCRIPTOR_ID_COLUMN_NAME}'.")),
        tuple.asString(DESCRIPTOR_COLUMN_NAME) ?: throw IllegalArgumentException("The provided tuple is missing the required field '$DESCRIPTOR_COLUMN_NAME'."),
        tuple.asFloat(CONFIDENCE_COLUMN_NAME) ?: throw IllegalArgumentException("The provided tuple is missing the required field '$DESCRIPTOR_COLUMN_NAME'.") /* TODO: Use UUID once supported. */
    )
}