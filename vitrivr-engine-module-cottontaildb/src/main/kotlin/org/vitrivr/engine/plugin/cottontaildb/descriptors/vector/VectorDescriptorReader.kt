package org.vitrivr.engine.plugin.cottontaildb.descriptors.vector

import org.vitrivr.cottontail.client.language.basics.Direction
import org.vitrivr.cottontail.client.language.basics.Distances
import org.vitrivr.cottontail.core.tuple.Tuple
import org.vitrivr.engine.core.model.descriptor.vector.*
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.query.Query
import org.vitrivr.engine.core.model.query.proximity.ProximityQuery
import org.vitrivr.engine.core.model.retrievable.Retrieved
import org.vitrivr.engine.core.model.retrievable.attributes.DistanceAttribute
import org.vitrivr.engine.core.model.types.toValue
import org.vitrivr.engine.plugin.cottontaildb.*
import org.vitrivr.engine.plugin.cottontaildb.descriptors.AbstractDescriptorReader

/**
 * An [AbstractDescriptorReader] for [FloatVectorDescriptor]s.
 *
 * @author Ralph Gasser
 * @version 1.1.0
 */
internal class VectorDescriptorReader(field: Schema.Field<*, VectorDescriptor<*>>, connection: CottontailConnection) : AbstractDescriptorReader<VectorDescriptor<*>>(field, connection) {

    /** The [VectorDescriptor] prototype handled by this [VectorDescriptorReader]. */
    private val prototype: VectorDescriptor<*> = this.field.analyser.prototype(this.field)

    /**
     * Executes the provided [Query] and returns a [Sequence] of [Retrieved]s that match it.
     *
     * @param query The [Query] to execute.
     */
    override fun getAll(query: Query): Sequence<Retrieved> = when (query) {
        is ProximityQuery<*> -> {
            val cottontailQuery = org.vitrivr.cottontail.client.language.dql.Query(this.entityName)
                .select(RETRIEVABLE_ID_COLUMN_NAME)
                .distance(
                    DESCRIPTOR_COLUMN_NAME,
                    query.value.toCottontailValue(),
                    Distances.valueOf(query.distance.toString()),
                    DISTANCE_COLUMN_NAME
                )
                .order(DISTANCE_COLUMN_NAME, Direction.valueOf(query.order.name))
                .limit(query.k.toLong())

            if (query.fetchVector) {
                cottontailQuery.select(DESCRIPTOR_COLUMN_NAME)
                cottontailQuery.select(DESCRIPTOR_ID_COLUMN_NAME)
            }

            this.connection.client.query(cottontailQuery).asSequence().mapNotNull {
                val retrievableId = it.asUuidValue(RETRIEVABLE_ID_COLUMN_NAME)?.value ?: return@mapNotNull null
                val distance =
                    (it.asFloat(DISTANCE_COLUMN_NAME) ?: it.asDouble(DISTANCE_COLUMN_NAME)?.toFloat())?.let { f ->
                        if (f.isNaN()) Float.MAX_VALUE else f
                    } ?: return@mapNotNull null
                val retrieved = Retrieved(retrievableId, null, false)
                retrieved.addAttribute(DistanceAttribute(distance))
                if (query.fetchVector) {
                    val descriptor = tupleToDescriptor(it)
                    retrieved.addDescriptor(descriptor)
                }
                retrieved
            }
        }

        else -> throw UnsupportedOperationException("Query of typ ${query::class} is not supported by FloatVectorDescriptorReader.")
    }

    /**
     * Converts the provided [Tuple] to a [VectorDescriptor].
     *
     * @param tuple The [Tuple] to convert.
     * @return The resulting [VectorDescriptor].
     */
    override fun tupleToDescriptor(tuple: Tuple): VectorDescriptor<*> {
        val descriptorId = tuple.asUuidValue(DESCRIPTOR_ID_COLUMN_NAME)?.value
                ?: throw IllegalArgumentException("The provided tuple is missing the required field '${DESCRIPTOR_ID_COLUMN_NAME}'.")
        val retrievableId = tuple.asUuidValue(RETRIEVABLE_ID_COLUMN_NAME)?.value
                ?: throw IllegalArgumentException("The provided tuple is missing the required field '${RETRIEVABLE_ID_COLUMN_NAME}'.")
        return when (this.prototype) {
            is BooleanVectorDescriptor -> BooleanVectorDescriptor(
                descriptorId,
                retrievableId,
                tuple.asBooleanVector(DESCRIPTOR_COLUMN_NAME)?.map { it.toValue() }
                    ?: throw IllegalArgumentException("The provided tuple is missing the required field '$DESCRIPTOR_COLUMN_NAME'.")
            )

            is FloatVectorDescriptor -> FloatVectorDescriptor(
                descriptorId,
                retrievableId,
                tuple.asFloatVector(DESCRIPTOR_COLUMN_NAME)?.map { it.toValue() }
                    ?: throw IllegalArgumentException("The provided tuple is missing the required field '$DESCRIPTOR_COLUMN_NAME'.")
            )

            is DoubleVectorDescriptor -> DoubleVectorDescriptor(
                descriptorId,
                retrievableId,
                tuple.asDoubleVector(DESCRIPTOR_COLUMN_NAME)?.map { it.toValue() }
                    ?: throw IllegalArgumentException("The provided tuple is missing the required field '$DESCRIPTOR_COLUMN_NAME'.")
            )

            is IntVectorDescriptor -> IntVectorDescriptor(
                descriptorId,
                retrievableId,
                tuple.asIntVector(DESCRIPTOR_COLUMN_NAME)?.map { it.toValue() }
                    ?: throw IllegalArgumentException("The provided tuple is missing the required field '$DESCRIPTOR_COLUMN_NAME'.")
            )

            is LongVectorDescriptor -> LongVectorDescriptor(
                descriptorId,
                retrievableId,
                tuple.asLongVector(DESCRIPTOR_COLUMN_NAME)?.map { it.toValue() }
                    ?: throw IllegalArgumentException("The provided tuple is missing the required field '$DESCRIPTOR_COLUMN_NAME'.")
            )
        }
    }
}