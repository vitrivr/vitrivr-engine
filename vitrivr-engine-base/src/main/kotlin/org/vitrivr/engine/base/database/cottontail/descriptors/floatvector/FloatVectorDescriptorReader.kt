package org.vitrivr.engine.base.database.cottontail.descriptors.floatvector

import org.vitrivr.cottontail.client.language.basics.Direction
import org.vitrivr.cottontail.client.language.basics.Distances
import org.vitrivr.cottontail.core.tuple.Tuple
import org.vitrivr.cottontail.core.values.FloatVectorValue
import org.vitrivr.engine.base.database.cottontail.CottontailConnection
import org.vitrivr.engine.base.database.cottontail.CottontailConnection.Companion.DESCRIPTOR_ID_COLUMN_NAME
import org.vitrivr.engine.base.database.cottontail.CottontailConnection.Companion.RETRIEVABLE_ID_COLUMN_NAME
import org.vitrivr.engine.base.database.cottontail.reader.AbstractDescriptorReader
import org.vitrivr.engine.core.model.database.descriptor.vector.FloatVectorDescriptor
import org.vitrivr.engine.core.model.database.retrievable.ScoredRetrievable
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.query.Query
import org.vitrivr.engine.core.model.query.proximity.ProximityQuery
import java.util.*

/**
 * An [AbstractDescriptorReader] for [FloatVectorDescriptor]s.
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
internal class FloatVectorDescriptorReader(field: Schema.Field<*,FloatVectorDescriptor>, connection: CottontailConnection): AbstractDescriptorReader<FloatVectorDescriptor>(field, connection) {
    /**
     * Executes the provided [Query] and returns a [Sequence] of [ScoredRetrievable]s that match it.
     *
     * @param query The [Query] to execute.
     */
    override fun getAll(query: Query<FloatVectorDescriptor>): Sequence<ScoredRetrievable> = when(query) {
        is ProximityQuery<FloatVectorDescriptor> -> {
            val cottontailQuery = org.vitrivr.cottontail.client.language.dql.Query(this.entityName)
                .select(RETRIEVABLE_ID_COLUMN_NAME)
                .distance(FEATURE_COLUMN_NAME, FloatVectorValue(query.descriptor.vector.toTypedArray()), Distances.valueOf(query.distance.toString()), FEATURE_COLUMN_NAME)
                .order(DISTANCE_COLUMN_NAME, Direction.valueOf(query.order.name))
                .limit(query.k.toLong())
            this.connection.client.query(cottontailQuery).asSequence().mapNotNull {
                val retrievableId = it.asString(RETRIEVABLE_ID_COLUMN_NAME) ?: return@mapNotNull null
                val score = it.asFloat(DISTANCE_COLUMN_NAME) ?: return@mapNotNull null
                ScoredRetrievable.Default(UUID.fromString(retrievableId), null, false, emptySet(), emptySet(), score, emptyMap()) /* TODO: Use UUID type once supported. */
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
    override fun tupleToDescriptor(tuple: Tuple): FloatVectorDescriptor = FloatVectorDescriptor(
        UUID.fromString(tuple.asString(RETRIEVABLE_ID_COLUMN_NAME) ?: throw IllegalArgumentException("The provided tuple is missing the required field '${RETRIEVABLE_ID_COLUMN_NAME}'.")),
        UUID.fromString(tuple.asString(DESCRIPTOR_ID_COLUMN_NAME) ?: throw IllegalArgumentException("The provided tuple is missing the required field '${DESCRIPTOR_ID_COLUMN_NAME}'.")),
        tuple.asFloatVector(FEATURE_COLUMN_NAME)?.toList() ?: throw IllegalArgumentException("The provided tuple is missing the required field '$FEATURE_COLUMN_NAME'.")
    )
}