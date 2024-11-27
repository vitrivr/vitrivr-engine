package org.vitrivr.engine.database.pgvector.descriptor.vector

import org.vitrivr.engine.core.database.descriptor.DescriptorReader
import org.vitrivr.engine.core.model.descriptor.vector.*
import org.vitrivr.engine.core.model.descriptor.vector.VectorDescriptor.Companion.VECTOR_ATTRIBUTE_NAME
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.query.Predicate
import org.vitrivr.engine.core.model.query.Query
import org.vitrivr.engine.core.model.query.proximity.ProximityPredicate
import org.vitrivr.engine.core.model.retrievable.Retrieved
import org.vitrivr.engine.core.model.retrievable.attributes.DistanceAttribute
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
class VectorDescriptorReader(field: Schema.Field<*, VectorDescriptor<*, *>>, connection: PgVectorConnection) : AbstractDescriptorReader<VectorDescriptor<*, *>>(field, connection) {
    /**
     * Executes the provided [Query] and returns a [Sequence] of [Retrieved]s that match it.
     *
     * @param query The [Query] to execute.
     */
    override fun query(query: Query): Sequence<VectorDescriptor<*, *>> = when (val predicate = query.predicate) {
        is ProximityPredicate<*> -> queryProximity(predicate)
        else -> throw UnsupportedOperationException("Query of typ ${query::class} is not supported by VectorDescriptorReader.")
    }

    /**
     * Returns a [Sequence] of all [Retrieved]s that match the given [Predicate].
     *
     * Implicitly, this methods executes a [query] and then JOINS the result with the [Retrieved]s.
     *
     * @param query The [Query] that should be executed.
     * @return [Sequence] of [Retrieved].
     */
    override fun queryAndJoin(query: Query): Sequence<Retrieved> = when (val predicate = query.predicate) {
        is ProximityPredicate<*> -> queryAndJoinProximity(predicate)
        else -> super.queryAndJoin(query)
    }

    /**
     * Converts a [ResultSet] to a [VectorDescriptor].
     *
     * @param result [ResultSet] to convert.
     * @return [VectorDescriptor]
     */
    override fun rowToDescriptor(result: ResultSet): VectorDescriptor<*, *> {
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
     * Executes a [ProximityPredicate] and returns a [Sequence] of [VectorDescriptor]s.
     *
     * @param query The [ProximityPredicate] to execute.
     * @return [Sequence] of [VectorDescriptor]s.
     */
    private fun queryProximity(query: ProximityPredicate<*>): Sequence<VectorDescriptor<*, *>> = sequence {
        val statement =
            "SELECT $DESCRIPTOR_ID_COLUMN_NAME, $RETRIEVABLE_ID_COLUMN_NAME, $VECTOR_ATTRIBUTE_NAME, $VECTOR_ATTRIBUTE_NAME ${query.distance.toSql()} ? AS $DISTANCE_COLUMN_NAME FROM \"${tableName.lowercase()}\" ORDER BY $DISTANCE_COLUMN_NAME ${query.order} LIMIT ${query.k}"
        this@VectorDescriptorReader.connection.jdbc.prepareStatement(statement).use { stmt ->
            stmt.setValue(1, query.value)
            stmt.executeQuery().use { result ->
                 while (result.next()) {
                    yield(rowToDescriptor(result))
                 }
            }
        }
    }

    /**
     * Executes a [ProximityPredicate] and returns a [Sequence] of [VectorDescriptor]s.
     *
     * @param query The [ProximityPredicate] to execute.
     * @return [Sequence] of [VectorDescriptor]s.
     */
    private fun queryAndJoinProximity(query: ProximityPredicate<*>): Sequence<Retrieved> = sequence {
        val cteTable = "\"${this@VectorDescriptorReader.tableName}_nns\""
        val statement = "WITH $cteTable AS (" +
                "SELECT \"$DESCRIPTOR_ID_COLUMN_NAME\",\"$RETRIEVABLE_ID_COLUMN_NAME\",\"$VECTOR_ATTRIBUTE_NAME\",\"$VECTOR_ATTRIBUTE_NAME\" ${query.distance.toSql()} ? AS $DISTANCE_COLUMN_NAME " +
                "FROM \"${tableName.lowercase()}\" ORDER BY \"$DISTANCE_COLUMN_NAME\" ${query.order} " +
                "LIMIT ${query.k}" +
                ") SELECT $cteTable.\"$DESCRIPTOR_ID_COLUMN_NAME\",$cteTable.\"$RETRIEVABLE_ID_COLUMN_NAME\",$cteTable.\"$VECTOR_ATTRIBUTE_NAME\",$cteTable.\"$DISTANCE_COLUMN_NAME\",\"$RETRIEVABLE_TYPE_COLUMN_NAME\" " +
                "FROM $cteTable INNER JOIN \"$RETRIEVABLE_ENTITY_NAME\" ON (\"$RETRIEVABLE_ENTITY_NAME\".\"$RETRIEVABLE_ID_COLUMN_NAME\" = $cteTable.\"$RETRIEVABLE_ID_COLUMN_NAME\")" +
                "ORDER BY $cteTable.\"$DISTANCE_COLUMN_NAME\" ${query.order}"

        this@VectorDescriptorReader.connection.jdbc.prepareStatement(statement).use { stmt ->
            stmt.setValue(1, query.value)
            stmt.executeQuery().use { result ->
                while (result.next()) {
                    val retrieved = Retrieved(result.getObject(RETRIEVABLE_ID_COLUMN_NAME, UUID::class.java), result.getString(RETRIEVABLE_TYPE_COLUMN_NAME), true)
                    retrieved.addAttribute(DistanceAttribute(result.getFloat(DISTANCE_COLUMN_NAME)))
                    if (query.fetchVector) {
                        retrieved.addDescriptor(rowToDescriptor(result))
                    }
                    yield(retrieved)
                }
            }
        }
    }
}