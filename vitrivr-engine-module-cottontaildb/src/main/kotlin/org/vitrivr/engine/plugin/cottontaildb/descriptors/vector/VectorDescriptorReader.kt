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
 * @version 1.2.0
 */
internal class VectorDescriptorReader(field: Schema.Field<*, VectorDescriptor<*>>, connection: CottontailConnection) : AbstractDescriptorReader<VectorDescriptor<*>>(field, connection) {

    /** The [VectorDescriptor] prototype handled by this [VectorDescriptorReader]. */
    private val prototype: VectorDescriptor<*> = this.field.analyser.prototype(this.field)

    /**
     * Executes the provided [Query] and returns a [Sequence] of [Retrieved]s that match it.
     *
     * @param query The [Query] to execute.
     */
    override fun query(query: Query): Sequence<VectorDescriptor<*>> = when (query) {
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
                .limit(query.k)

            if (query.fetchVector) {
                cottontailQuery.select(DESCRIPTOR_COLUMN_NAME)
                cottontailQuery.select(DESCRIPTOR_ID_COLUMN_NAME)
            }

            this.connection.client.query(cottontailQuery).asSequence().map {
                tupleToDescriptor(it)
            }
        }

        else -> throw UnsupportedOperationException("Query of typ ${query::class} is not supported by FloatVectorDescriptorReader.")
    }

    /**
     * Returns a [Sequence] of all [Retrieved]s that match the given [Query].
     *
     * Implicitly, this methods executes a [query] and then JOINS the result with the [Retrieved]s.
     *
     * @param query The [Query] that should be executed.
     * @return [Sequence] of [Retrieved].
     */
    override fun queryAndJoin(query: Query): Sequence<Retrieved> {
        when (query) {
            is ProximityQuery<*> -> {
                val cottontailQuery = org.vitrivr.cottontail.client.language.dql.Query(this.entityName)
                    .select(DESCRIPTOR_COLUMN_NAME)
                    .select(DESCRIPTOR_ID_COLUMN_NAME)
                    .select(RETRIEVABLE_ID_COLUMN_NAME)
                    .distance(
                        DESCRIPTOR_COLUMN_NAME,
                        query.value.toCottontailValue(),
                        Distances.valueOf(query.distance.toString()),
                        DISTANCE_COLUMN_NAME
                    )
                    .order(DISTANCE_COLUMN_NAME, Direction.valueOf(query.order.name))
                    .limit(query.k)

                /* Fetch descriptors */
                val descriptors = this.connection.client.query(cottontailQuery).asSequence().map { tuple ->
                    val scoreIndex = tuple.indexOf(DISTANCE_COLUMN_NAME)
                    tupleToDescriptor(tuple) to if (scoreIndex > -1) {
                        tuple.asDouble(DISTANCE_COLUMN_NAME)?.let { DistanceAttribute(it.toFloat()) }
                    } else {
                        null
                    }
                }.toList()
                if (descriptors.isEmpty()) return emptySequence()

                /* Fetch retrievable ids. */
                val retrievables = this.fetchRetrievable(descriptors.mapNotNull { it.first.retrievableId }.toSet())
                return descriptors.asSequence().mapNotNull { descriptor ->
                    val retrievable = retrievables[descriptor.first.retrievableId] ?: return@mapNotNull null

                    /* Append descriptor and distance attribute. */
                    retrievable.addDescriptor(descriptor.first)
                    descriptor.second?.let { retrievable.addAttribute(it) }
                    retrievable
                }
            }

            else -> throw UnsupportedOperationException("Query of typ ${query::class} is not supported by FloatVectorDescriptorReader.")
        }
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