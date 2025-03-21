package org.vitrivr.engine.database.pgvector.tables.scalar

import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Query
import org.jetbrains.exposed.sql.statements.BatchInsertStatement
import org.jetbrains.exposed.sql.statements.InsertStatement
import org.jetbrains.exposed.sql.statements.UpdateStatement
import org.vitrivr.engine.core.model.descriptor.Descriptor
import org.vitrivr.engine.core.model.descriptor.scalar.ScalarDescriptor
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.query.bool.SimpleBooleanQuery
import org.vitrivr.engine.core.model.types.Value
import org.vitrivr.engine.database.pgvector.tables.AbstractDescriptorTable

/**
 * An [AbstractDescriptorTable] for [ScalarDescriptor]s.
 *
 * This class is used to define the table structure for scalar descriptors in the database.
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
sealed class AbstractScalarDescriptorTable<D: ScalarDescriptor<D, V>, V : Value.ScalarValue<S>, S>(field: Schema.Field<*, D>): AbstractDescriptorTable<D>(field) {
    /** The [Column] holding the scalar value. */
    abstract val descriptor: Column<S>

    /**
     * Converts a [org.vitrivr.engine.core.model.query.Query] into a [Query] that can be executed against the database.
     *
     * @param query The [org.vitrivr.engine.core.model.query.Query] to convert.
     * @return The [Query] that can be executed against the database.
     * @throws UnsupportedOperationException If the query is not supported.
     */
    override fun parse(query: org.vitrivr.engine.core.model.query.Query): Query = when(query) {
        is SimpleBooleanQuery<*> -> this.parse(query)
        else -> throw UnsupportedOperationException("Unsupported query type: ${query::class.simpleName}")
    }

    /**
     * Converts a [SimpleBooleanQuery] into a [Query] that can be executed against the database.
     *
     * @param query The [SimpleBooleanQuery] to convert.
     * @return The [Query] that can be executed against the database.
     * @throws UnsupportedOperationException If the query is not supported.
     */
    protected abstract fun parse(query: SimpleBooleanQuery<*>): Query

    /**
     * Sets the value of the descriptor in the [InsertStatement]t.
     *
     * @param d The [Descriptor] to set value for
     */
    override fun InsertStatement<*>.setValue(d: D) {
        this[descriptor] = d.value.value
    }

    /**
     * Sets the value of the descriptor in the [BatchInsertStatement]t.
     *
     * @param d The [Descriptor] to set value for
     */
    override fun BatchInsertStatement.setValue(d: D) {
        this[descriptor] = d.value.value
    }

    /**
     * Sets the value of the descriptor in the [UpdateStatement]t.
     *
     * @param d The [Descriptor] to set value for
     */
    override fun UpdateStatement.setValue(d: D) {
        this[descriptor] = d.value.value
    }
}