package org.vitrivr.engine.database.pgvector.tables

import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.statements.BatchInsertStatement
import org.jetbrains.exposed.sql.statements.InsertStatement
import org.jetbrains.exposed.sql.statements.UpdateStatement
import org.vitrivr.engine.core.model.descriptor.Descriptor
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.query.bool.SimpleBooleanQuery
import org.vitrivr.engine.core.model.query.fulltext.SimpleFulltextQuery
import org.vitrivr.engine.core.model.query.proximity.ProximityQuery
import org.vitrivr.engine.database.pgvector.DESCRIPTOR_ENTITY_PREFIX
import org.vitrivr.engine.database.pgvector.exposed.types.FloatVectorColumnType

/**
 * An abstract [UUIDTable] for [Descriptor]s. This class is used as a base class for all descriptor tables.
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
abstract class AbstractDescriptorTable<D : Descriptor<*>>(protected val field: Schema.Field<*, D>): UUIDTable("${DESCRIPTOR_ENTITY_PREFIX}_${field.fieldName.lowercase()}", "descriptorid") {
    /** Reference to the [RetrievableTable]. */
    val retrievableId = reference("retrievableid", RetrievableTable, onDelete = ReferenceOption.CASCADE).index()

    /** The prototype value handled by this [StructDescriptorTable]. */
    protected val prototype by lazy { this.field.getPrototype() }

    /**
     * Registers a new [FloatVectorColumnType].
     *
     * @param name The name of the [FloatVectorColumnType]
     * @param dimension The dimensionality of the [FloatVectorColumnType]
     */
    protected fun floatVector(name: String, dimension: Int) = registerColumn(name, FloatVectorColumnType(dimension))

    /**
     * Inserts a descriptor into the table. This method is used to insert a descriptor into the table.
     */
    fun insert(descriptor: D) = this.insert {
        it[this.id] = descriptor.id
        it[this.retrievableId] = descriptor.retrievableId ?: error("Cannot insert entity without retrievableId.")
        it.setValue(descriptor)
    }

    /**
     * Inserts a descriptor into the table. This method is used to insert a descriptor into the table.
     */
    fun update(descriptor: D) = this.update({ this@AbstractDescriptorTable.id eq descriptor.id }) {
        it[this.retrievableId] = descriptor.retrievableId ?: error("Cannot insert entity without retrievableId.")
        it.setValue(descriptor)
    }

    /**
     * Inserts a descriptor into the table. This method is used to insert a descriptor into the table.
     */
    fun batchInsert(entities: Iterable<D>) = this.batchInsert(entities) { descriptor ->
        this[id] = descriptor.id
        this[retrievableId] = descriptor.retrievableId ?: error("Cannot insert entity without retrievableId.")
        this.setValue(descriptor)
    }

    /**
     * Converts a [ProximityQuery] into a [Query] that can be executed against the database.
     *
     * @param query The [ProximityQuery] to convert.
     * @return The [Query] that can be executed against the database.
     */
    open fun parseQuery(query: ProximityQuery<*>): Query
        = throw UnsupportedOperationException("Proximity query is not supported on table '${this.nameInDatabaseCase()}'.")

    /**
     * Converts a [SimpleFulltextQuery] into a [Query] that can be executed against the database.
     *
     * @param query The [SimpleFulltextQuery] to convert.
     * @return The [Query] that can be executed against the database.
     */
    open fun parseQuery(query: SimpleFulltextQuery): Query
        = throw UnsupportedOperationException("Fulltext query is not supported on table '${this.nameInDatabaseCase()}'.")

    /**
     * Converts a [SimpleBooleanQuery] into a [Query] that can be executed against the database.
     *
     * @param query The [SimpleBooleanQuery] to convert.
     * @return The [Query] that can be executed against the database.
     */
    open fun parseQuery(query: SimpleBooleanQuery<*>): Query
        = throw UnsupportedOperationException("Simple Boolean query is not supported on table '${this.nameInDatabaseCase()}'.")

    /**
     * Converts a [ResultRow] to a [Descriptor].
     *
     * @param row The [ResultRow] to convert.
     * @return The [Descriptor] represented by the [ResultRow].
     */
    abstract fun rowToDescriptor(row: ResultRow): D

    /**
     * Sets the value of the descriptor in the [InsertStatement]t.
     *
     * @param d The [Descriptor] to set value for
     */
    protected abstract fun InsertStatement<*>.setValue(d: D)

    /**
     * Sets the value of the descriptor in the [UpdateStatement]t.
     *
     * @param d The [Descriptor] to set value for
     */
    protected abstract fun UpdateStatement.setValue(d: D)

    /**
     * Sets the value of the descriptor in the [BatchInsertStatement]t.
     *
     * @param d The [Descriptor] to set value for
     */
    protected abstract fun BatchInsertStatement.setValue(d: D)
}