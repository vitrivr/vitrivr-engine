package org.vitrivr.engine.database.pgvector.descriptor

import org.jetbrains.exposed.sql.Query
import org.jetbrains.exposed.sql.innerJoin
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.vitrivr.engine.core.database.descriptor.DescriptorReader
import org.vitrivr.engine.core.model.descriptor.Descriptor
import org.vitrivr.engine.core.model.descriptor.DescriptorId
import org.vitrivr.engine.core.model.descriptor.struct.StructDescriptor
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.retrievable.RetrievableId
import org.vitrivr.engine.core.model.retrievable.Retrieved
import org.vitrivr.engine.core.model.retrievable.attributes.DistanceAttribute
import org.vitrivr.engine.core.model.retrievable.attributes.RetrievableAttribute
import org.vitrivr.engine.database.pgvector.LOGGER
import org.vitrivr.engine.database.pgvector.PgVectorConnection
import org.vitrivr.engine.database.pgvector.exposed.ops.DistanceOps
import org.vitrivr.engine.database.pgvector.tables.AbstractDescriptorTable
import org.vitrivr.engine.database.pgvector.tables.RetrievableTable
import org.vitrivr.engine.database.pgvector.tables.RetrievableTable.toRetrieved
import org.vitrivr.engine.database.pgvector.toTable
import java.util.*

/**
 * An abstract implementation of a [DescriptorReader] for Cottontail DB.
 *
 * @author Ralph Gasser
 * @version 1.1.0
 */
class PgDescriptorReader<D : Descriptor<*>>(override val field: Schema.Field<*, D>, override val connection: PgVectorConnection) : DescriptorReader<D> {

    /** The [AbstractDescriptorTable] backing this [PgDescriptorReader]. */
    private val table: AbstractDescriptorTable<D> = this.field.toTable()

    /** The name of the table backing this [PgDescriptorReader]. */
    private val tableName: String
        get() = this.table.nameInDatabaseCase()

    /**
     * Executes the provided [Query] and returns a [Sequence] of [Retrieved]s that match it.
     *
     * @param query The [Query] to execute.
     * @return [Sequence] of [StructDescriptor]s that match the query.
     */
    override fun query(query: org.vitrivr.engine.core.model.query.Query): Sequence<D> = transaction(this.connection.database) {
        try {
            this@PgDescriptorReader.table.parse(query).map { this@PgDescriptorReader.table.rowToDescriptor(it) }.asSequence()
        } catch (e: Throwable) {
            LOGGER.error(e) { "Failed to execute query on '$tableName' due to error." }
            throw e
        }
    }

    /**
     * Returns a single [Descriptor]s of type [D] that has the provided [DescriptorId].
     *
     * @param descriptorId The [DescriptorId], i.e., the [UUID] of the [Descriptor] to return.
     * @return [Sequence] of all [Descriptor]s.
     */
    override fun get(descriptorId: DescriptorId): D? = transaction(this.connection.database) {
        try {
            this@PgDescriptorReader.table.selectAll().where {
                this@PgDescriptorReader.table.id eq descriptorId
            }.map {
                this@PgDescriptorReader.table.rowToDescriptor(it)
            }.firstOrNull()
        } catch (e: Throwable) {
            LOGGER.error(e) { "Failed to fetch descriptor $descriptorId from '$tableName' due to error." }
            null
        }
    }

    /**
     * Returns the [Descriptor]s of type [D] that belong to the provided [RetrievableId].
     *
     * @param retrievableId The [RetrievableId] to search for.
     * @return [Sequence] of [Descriptor]  of type [D]
     */
    override fun getForRetrievable(retrievableId: RetrievableId): Sequence<D> = transaction(this.connection.database) {
        try {
            this@PgDescriptorReader.table.selectAll().where {
                this@PgDescriptorReader.table.retrievableId eq retrievableId
            }.map { row ->
                this@PgDescriptorReader.table.rowToDescriptor(row)
            }.asSequence()
        } catch (e: Throwable) {
            LOGGER.error(e) { "Failed to fetch descriptor for retrievable $retrievableId from '$tableName' due to error." }
            throw e
        }
    }

    /**
     * Checks whether a [Descriptor] of type [D] with the provided [UUID] exists.
     *
     * @param descriptorId The [DescriptorId], i.e., the [UUID] of the [Descriptor] to check for.
     * @return True if descriptor exsists, false otherwise
     */
    override fun exists(descriptorId: DescriptorId): Boolean = transaction(this.connection.database) {
        try {
            !this@PgDescriptorReader.table.selectAll().where {
                this@PgDescriptorReader.table.id eq descriptorId
            }.empty()
        } catch (e: Throwable) {
            LOGGER.error(e) { "Failed to check for descriptor $descriptorId from '$tableName' due to error." }
            false
        }
    }

    /**
     * Returns all [Descriptor]s of type [D] as a [Sequence].
     *
     * @return [Sequence] of all [Descriptor]s.
     */
    override fun getAll(): Sequence<D> = transaction(this.connection.database) {
        try {
            this@PgDescriptorReader.table.selectAll().map { row ->
                this@PgDescriptorReader.table.rowToDescriptor(row)
            }.asSequence()
        } catch (e: Throwable) {
            LOGGER.error(e) { "Failed to fetch descriptors from '$tableName' due to error." }
            throw e
        }
    }

    /**
     * Returns a [Sequence] of all [Descriptor]s whose [DescriptorId] is contained in the provided [Iterable].
     *
     * @param descriptorIds A [Iterable] of [DescriptorId]s to return.
     * @return [Sequence] of [Descriptor] of type [D]
     */
    override fun getAll(descriptorIds: Iterable<DescriptorId>): Sequence<D> = transaction(this.connection.database) {
        try {
            this@PgDescriptorReader.table.selectAll().where {
                this@PgDescriptorReader.table.id inList descriptorIds
            }.map { row ->
                this@PgDescriptorReader.table.rowToDescriptor(row)
            }.asSequence()
        } catch (e: Throwable) {
            LOGGER.error(e) { "Failed to fetch descriptors from '$tableName' due to error." }
            throw e
        }
    }

    /**
     * Returns a [Sequence] of all [Descriptor] whose [RetrievableId] is contained in the provided [Iterable].
     *
     * @param retrievableIds A [Iterable] of [RetrievableId]s to return [Descriptor]s for
     * @return [Sequence] of [Descriptor] of type [D]
     */
    override fun getAllForRetrievable(retrievableIds: Iterable<RetrievableId>): Sequence<D> = transaction(this.connection.database) {
        try {
            this@PgDescriptorReader.table.selectAll().where {
                this@PgDescriptorReader.table.retrievableId inList retrievableIds
            }.map { row ->
                this@PgDescriptorReader.table.rowToDescriptor(row)
            }.asSequence()
        } catch (e: Throwable) {
            LOGGER.error(e) { "Failed to fetch descriptors from '$tableName' due to error." }
            throw e
        }
    }

    /**
     * Returns the number of [Descriptor]s contained in the entity managed by this [PgDescriptorReader]
     *
     * @return [Sequence] of all [Descriptor]s.
     */
    override fun count(): Long = transaction(this.connection.database) {
        try {
            this@PgDescriptorReader.table.selectAll().count()
        } catch (e: Throwable) {
            LOGGER.error(e) { "Failed to count descriptors from '$tableName' due to error." }
            0L
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
    override fun queryAndJoin(query: org.vitrivr.engine.core.model.query.Query) = transaction(this.connection.database) {
        val sqlQuery = this@PgDescriptorReader.table.parse(query)
        sqlQuery.adjustColumnSet {
            innerJoin(RetrievableTable, { this@PgDescriptorReader.table.retrievableId }, { RetrievableTable.id })
        }
        sqlQuery.adjustSelect {
            select(sqlQuery.set.fields + RetrievableTable.columns)
        }

        val grouped = sqlQuery.groupBy { it[this@PgDescriptorReader.table.retrievableId] }
        grouped.map { (_, rows) ->
            val descriptors = mutableSetOf<Descriptor<*>>()
            val attributes = mutableSetOf<RetrievableAttribute>()
            for (row in rows) {
                val distance = row.fieldIndex.keys.filterIsInstance<DistanceOps>().firstOrNull()
                if (distance != null) {
                    attributes.add(DistanceAttribute.Local(row[distance], row[this@PgDescriptorReader.table.id].value))
                }
                try {
                    descriptors.add(this@PgDescriptorReader.table.rowToDescriptor(row))
                } catch (e: Throwable) {
                    /* No operation */
                }
            }
            rows.first().toRetrieved(descriptors, attributes)
        }.asSequence()
    }
}