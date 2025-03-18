package org.vitrivr.engine.database.pgvector.tables.vector

import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Query
import org.jetbrains.exposed.sql.ResultRow
import org.vitrivr.engine.core.model.descriptor.vector.FloatVectorDescriptor
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.query.basics.Distance
import org.vitrivr.engine.core.model.query.proximity.ProximityQuery
import org.vitrivr.engine.core.model.types.Value
import org.vitrivr.engine.database.pgvector.exposed.cosine
import org.vitrivr.engine.database.pgvector.exposed.euclidean
import org.vitrivr.engine.database.pgvector.exposed.inner
import org.vitrivr.engine.database.pgvector.exposed.manhattan
import org.vitrivr.engine.database.pgvector.toSql

/**
 * A [AbstractVectorDescriptorTable] for [FloatVectorDescriptor]s.
 *
 * This class is used to define the table structure for float vector descriptors in the database.
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
class FloatVectorDescriptorTable(field: Schema.Field<*, FloatVectorDescriptor>): AbstractVectorDescriptorTable<FloatVectorDescriptor, Value.FloatVector, FloatArray>(field) {
    /** The [Column] holding the vector value. */
    override val descriptor: Column<FloatArray> = floatVector("descriptor", this.prototype.dimensionality)

    /**
     * Converts a [ProximityQuery] into a [Query] that can be executed against the database.
     *
     * @param query The [ProximityQuery] to convert.
     * @return The [Query] that can be executed against the database.
     */
    override fun parseQuery(query: ProximityQuery<*>): Query {
        val value = query.value.value as? FloatArray ?: throw IllegalArgumentException("Failed to execute query on ${nameInDatabaseCase()}. Comparison value of wrong type.")
        val expression = when (query.distance) {
            Distance.EUCLIDEAN -> descriptor euclidean value
            Distance.MANHATTAN -> descriptor manhattan  value
            Distance.COSINE -> descriptor cosine value
            Distance.INNER -> descriptor inner value
            else -> throw IllegalArgumentException("Unsupported distance type: ${query.distance}")
        }

        /* Prepare query. */
        return if (query.fetchVector) {
            this.select(this.id, this.retrievableId, this.descriptor, expression)
        } else {
            this.select(this.id, this.retrievableId, expression)
        }.orderBy(expression, query.order.toSql()).limit(query.k.toInt())
    }

    /**
     * Converts a [ResultRow] to a [FloatVectorDescriptor].
     *
     * @param row The [ResultRow] to convert.
     * @return The [FloatVectorDescriptor] represented by the [ResultRow].
     */
    override fun rowToDescriptor(row: ResultRow) = FloatVectorDescriptor(
        id = row[id].value,
        retrievableId = row[retrievableId].value,
        vector = Value.FloatVector(row[descriptor]),
        this.field
    )
}