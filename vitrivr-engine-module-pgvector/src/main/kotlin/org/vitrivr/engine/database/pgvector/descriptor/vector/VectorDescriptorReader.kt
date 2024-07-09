package org.vitrivr.engine.database.pgvector.descriptor.vector

import org.vitrivr.engine.core.database.descriptor.DescriptorReader
import org.vitrivr.engine.core.model.descriptor.vector.*
import org.vitrivr.engine.core.model.descriptor.vector.VectorDescriptor.Companion.VECTOR_ATTRIBUTE_NAME
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.query.Query
import org.vitrivr.engine.core.model.query.basics.Distance
import org.vitrivr.engine.core.model.query.basics.Distance.*
import org.vitrivr.engine.core.model.query.proximity.ProximityQuery
import org.vitrivr.engine.core.model.retrievable.Retrieved
import org.vitrivr.engine.core.model.retrievable.attributes.DistanceAttribute
import org.vitrivr.engine.core.model.types.Value
import org.vitrivr.engine.database.pgvector.*
import org.vitrivr.engine.database.pgvector.descriptor.AbstractDescriptorReader
import org.vitrivr.engine.database.pgvector.descriptor.model.PgBitVector
import org.vitrivr.engine.database.pgvector.descriptor.model.PgVector
import java.sql.ResultSet
import java.util.*

/**
 * An abstract implementation of a [DescriptorReader] for Cottontail DB.
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
class VectorDescriptorReader(field: Schema.Field<*, VectorDescriptor<*>>, connection: PgVectorConnection) : AbstractDescriptorReader<VectorDescriptor<*>>(field, connection) {
    /**
     * Executes the provided [Query] and returns a [Sequence] of [Retrieved]s that match it.
     *
     * @param query The [Query] to execute.
     */
    override fun query(query: Query): Sequence<VectorDescriptor<*>> {
        try {
            val statement = when (query) {
                is ProximityQuery<*> -> this.connection.jdbc.prepareStatement("SELECT *, $VECTOR_ATTRIBUTE_NAME ${query.distance.operator()} ? AS $DISTANCE_COLUMN_NAME FROM $tableName ORDER BY $DISTANCE_COLUMN_NAME LIMIT ${query.k}").apply {
                    when (val vector = query.value) {
                        is Value.FloatVector -> this.setObject(1, PgVector(vector.value))
                        is Value.BooleanVector -> this.setObject(1, PgBitVector(vector.value))
                        else -> throw IllegalArgumentException("Unsupported value type ${query.value::class}.")
                    }
                }
                else -> throw UnsupportedOperationException("Query of typ ${query::class} is not supported by VectorDescriptorReader.")
            }
            return statement.executeAndStream()
        } catch (e: Exception) {
            LOGGER.error(e) { "Failed to execute query on '$tableName' due to SQL error." }
            return emptySequence()
        }
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
        try {
            return when (query) {
                is ProximityQuery<*> -> {
                    val descriptors = mutableListOf<Pair<VectorDescriptor<*>, Float>>()
                    this.connection.jdbc.prepareStatement("SELECT *, $VECTOR_ATTRIBUTE_NAME ${query.distance.operator()} ? AS $DISTANCE_COLUMN_NAME FROM $tableName ORDER BY $DISTANCE_COLUMN_NAME LIMIT ${query.k}").use { stmt ->
                        when (val vector = query.value) {
                            is Value.FloatVector -> stmt.setObject(1, PgVector(vector.value))
                            is Value.BooleanVector -> stmt.setObject(1, PgBitVector(vector.value))
                            else -> throw IllegalArgumentException("Unsupported value type ${query.value::class}.")
                        }
                        stmt.executeQuery().use { result ->
                            while (result.next()) {
                                descriptors.add(this@VectorDescriptorReader.rowToDescriptor(result) to result.getFloat(DISTANCE_COLUMN_NAME))
                            }
                        }
                    }

                    /* Fetch retrievable ids. */
                    val retrievables = this.connection.getRetrievableReader().getAll(descriptors.mapNotNull { it.first.retrievableId }.toSet()).map { it.id to it }.toMap()
                    descriptors.asSequence().mapNotNull { (descriptor, distance) ->
                        val retrievable = retrievables[descriptor.retrievableId]
                        if (retrievable != null) {
                            retrievable.addDescriptor(descriptor)
                            retrievable.addAttribute(DistanceAttribute(distance))
                            retrievable as Retrieved
                        } else {
                            null
                        }
                    }
                }
                else -> super.queryAndJoin(query)
            }
        } catch (e: Exception) {
            LOGGER.error(e) { "Failed to execute query on '$tableName' due to SQL error." }
            return emptySequence()
        }
    }

    /**
     * Converts a [ResultSet] to a [VectorDescriptor].
     *
     * @param result [ResultSet] to convert.
     * @return [VectorDescriptor]
     */
    override fun rowToDescriptor(result: ResultSet): VectorDescriptor<*> {
        val descriptorId = result.getObject(DESCRIPTOR_ID_COLUMN_NAME, UUID::class.java)
        val retrievableId = result.getObject(RETRIEVABLE_ID_COLUMN_NAME, UUID::class.java)
        return when (this.prototype) {
            is FloatVectorDescriptor -> FloatVectorDescriptor(
                descriptorId,
                retrievableId,
                result.getObject(VECTOR_ATTRIBUTE_NAME, PgVector::class.java)?.toFloatVector() ?: throw IllegalArgumentException("The provided vector value is missing the required field '$VECTOR_ATTRIBUTE_NAME'.")
            )

            is DoubleVectorDescriptor -> DoubleVectorDescriptor(
                descriptorId,
                retrievableId,
                result.getObject(VECTOR_ATTRIBUTE_NAME, PgVector::class.java)?.toDoubleVector() ?: throw IllegalArgumentException("The provided vector value is missing the required field '$VECTOR_ATTRIBUTE_NAME'.")
            )

            is IntVectorDescriptor -> IntVectorDescriptor(
                descriptorId,
                retrievableId,
                result.getObject(VECTOR_ATTRIBUTE_NAME, PgVector::class.java)?.toIntVector() ?: throw IllegalArgumentException("The provided vector value is missing the required field '$VECTOR_ATTRIBUTE_NAME'.")
            )

            is LongVectorDescriptor -> LongVectorDescriptor(
                descriptorId,
                retrievableId,
                result.getObject(VECTOR_ATTRIBUTE_NAME, PgVector::class.java)?.toLongVector() ?: throw IllegalArgumentException("The provided vector value is missing the required field '$VECTOR_ATTRIBUTE_NAME'.")
            )

            is BooleanVectorDescriptor -> BooleanVectorDescriptor(
                descriptorId,
                retrievableId,
                result.getObject(VECTOR_ATTRIBUTE_NAME, PgBitVector::class.java)?.toBooleanVector() ?: throw IllegalArgumentException("The provided vector value is missing the required field '$VECTOR_ATTRIBUTE_NAME'.")
            )

            else -> throw IllegalArgumentException("Unsupported descriptor type ${this.prototype::class}.")
        }
    }

    /**
     * Converts a [Distance] to a pgVector distance operator.
     */
    private fun Distance.operator() = when(this) {
        MANHATTAN -> "<+>"
        EUCLIDEAN -> "<->"
        COSINE -> "<=>"
        HAMMING -> "<~>"
        JACCARD -> "<%>"
    }
}