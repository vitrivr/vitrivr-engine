package org.vitrivr.engine.base.database.cottontail.descriptors.scalar

import org.vitrivr.cottontail.client.language.basics.expression.Column
import org.vitrivr.cottontail.client.language.basics.expression.Literal
import org.vitrivr.cottontail.client.language.basics.predicate.Compare
import org.vitrivr.cottontail.core.tuple.Tuple
import org.vitrivr.engine.base.database.cottontail.CottontailConnection
import org.vitrivr.engine.base.database.cottontail.CottontailConnection.Companion.DESCRIPTOR_ID_COLUMN_NAME
import org.vitrivr.engine.base.database.cottontail.CottontailConnection.Companion.RETRIEVABLE_ID_COLUMN_NAME
import org.vitrivr.engine.base.database.cottontail.descriptors.DESCRIPTOR_COLUMN_NAME
import org.vitrivr.engine.base.database.cottontail.descriptors.operator
import org.vitrivr.engine.base.database.cottontail.descriptors.toValue
import org.vitrivr.engine.base.database.cottontail.reader.AbstractDescriptorReader
import org.vitrivr.engine.core.model.database.descriptor.scalar.ScalarDescriptor
import org.vitrivr.engine.core.model.database.descriptor.scalar.StringDescriptor
import org.vitrivr.engine.core.model.database.descriptor.vector.FloatVectorDescriptor
import org.vitrivr.engine.core.model.database.retrievable.Retrieved
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.query.Query
import org.vitrivr.engine.core.model.query.bool.BooleanQuery
import java.util.*

/**
 * An [AbstractDescriptorReader] for [StringDescriptor]s.
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
class ScalarDescriptorReader(field: Schema.Field<*, ScalarDescriptor<*>>, connection: CottontailConnection) : AbstractDescriptorReader<ScalarDescriptor<*>>(field, connection) {
    /**
     * Executes the provided [Query] and returns a [Sequence] of [Retrieved.WithDescriptor]s that match it.
     *
     * @param query The [Query] to execute.
     */
    override fun getAll(query: Query<ScalarDescriptor<*>>): Sequence<Retrieved> = when (query) {
        is BooleanQuery<ScalarDescriptor<*>> -> {
            val column = this.entityName.column(DESCRIPTOR_COLUMN_NAME)
            val value = query.descriptor.toValue()
            val cottontailQuery = org.vitrivr.cottontail.client.language.dql.Query(this.entityName)
                .select(RETRIEVABLE_ID_COLUMN_NAME)
                .select(DESCRIPTOR_ID_COLUMN_NAME)
                .select(DESCRIPTOR_COLUMN_NAME)
                .where(Compare(Column(column), query.operator(), Literal(value)))
            this.connection.client.query(cottontailQuery).asSequence().map {
                val descriptor = this.tupleToDescriptor(it)
                Retrieved.WithDescriptor(descriptor.retrievableId, null, listOf(descriptor), false)
            }
        }

        else -> throw UnsupportedOperationException("Query of typ ${query::class} is not supported by FloatVectorDescriptorReader.")
    }

    /**
     * Converts the provided [Tuple] to a [FloatVectorDescriptor].
     *
     * @param tuple The [Tuple] to convert.
     * @return The resulting [FloatVectorDescriptor].
     */
    override fun tupleToDescriptor(tuple: Tuple): StringDescriptor = StringDescriptor(
        UUID.fromString(tuple.asString(RETRIEVABLE_ID_COLUMN_NAME) ?: throw IllegalArgumentException("The provided tuple is missing the required field '${RETRIEVABLE_ID_COLUMN_NAME}'.")),
        UUID.fromString(tuple.asString(DESCRIPTOR_ID_COLUMN_NAME) ?: throw IllegalArgumentException("The provided tuple is missing the required field '${DESCRIPTOR_ID_COLUMN_NAME}'.")),
        tuple.asString(DESCRIPTOR_COLUMN_NAME) ?: throw IllegalArgumentException("The provided tuple is missing the required field '$DESCRIPTOR_COLUMN_NAME'.") /* TODO: Use UUID once supported. */
    )
}