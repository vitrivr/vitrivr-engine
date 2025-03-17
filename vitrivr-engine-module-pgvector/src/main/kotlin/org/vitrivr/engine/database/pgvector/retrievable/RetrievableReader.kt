package org.vitrivr.engine.database.pgvector.retrievable

import org.jetbrains.exposed.sql.andWhere
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.vitrivr.engine.core.database.retrievable.RetrievableReader
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.model.retrievable.RetrievableId
import org.vitrivr.engine.database.pgvector.*
import org.vitrivr.engine.database.pgvector.exposed.RelationshipTable
import org.vitrivr.engine.database.pgvector.exposed.RelationshipTable.toRelationship
import org.vitrivr.engine.database.pgvector.exposed.RetrievableTable
import org.vitrivr.engine.database.pgvector.exposed.RetrievableTable.toRetrieved
import java.sql.SQLException
import java.util.*

/**
 * A [RetrievableReader] implementation for PostgreSQL with pgVector.
 *
 * @author Ralph Gasser
 * @version 1.1.0
 */
class RetrievableReader(override val connection: PgVectorConnection): RetrievableReader {
    /**
     * Returns the [Retrievable]s that matches the provided [RetrievableId]
     */
    override fun get(id: RetrievableId): Retrievable? = try {
        transaction(this.connection.database) {
            RetrievableTable.selectAll().where {
                RetrievableTable.id eq id
            }.map { it.toRetrieved() }.firstOrNull()
        }
    } catch (e: Throwable) {
        LOGGER.error(e) { "Failed to fetch retrievable $id due to error." }
        null
    }

    /**
     * Checks whether a [Retrievable] with the provided [RetrievableId] exists.
     *
     * @param id The [RetrievableId], i.e., the [UUID] of the [Retrievable] to check for.
     * @return True if descriptor exists, false otherwise
     */
    override fun exists(id: RetrievableId): Boolean = try {
        transaction(this.connection.database) {
            RetrievableTable.selectAll().where {
                RetrievableTable.id eq id
            }.count() > 0
        }
    } catch (e: Throwable) {
        LOGGER.error(e) { "Failed to check for retrievable $id due to error." }
        false
    }

    /**
     * Returns all [Retrievable]s that match any of the provided [RetrievableId]
     *
     * @param ids A [Iterable] of [RetrievableId]s to return.
     * @return A [Sequence] of all [Retrievable].
     */
    override fun getAll(ids: Iterable<RetrievableId>): Sequence<Retrievable> = sequenceWithTx(this.connection.database) {
        try {
            val query = RetrievableTable.selectAll().where { RetrievableTable.id inList ids }
            for (row in query) {
                yield(row.toRetrieved())
            }
        } catch (e: Throwable) {
            LOGGER.error(e) { "Failed to fetch retrievables due to SQL error." }
        }
    }

    /**
     * Returns all [Retrievable]s stored by the database.
     *
     * @return A [Sequence] of all [Retrievable]s in the database.
     */
    override fun getAll(): Sequence<Retrievable> = sequenceWithTx(this.connection.database)  {
        try {
            val query = RetrievableTable.selectAll()
            for (row in query) {
                yield(row.toRetrieved())
            }
        } catch (e: Throwable) {
            LOGGER.error(e) { "Failed to fetch retrievables due to SQL error." }
        }
    }

    /**
     * Returns all relationships thar are stored by the database and that match the given query.
     *
     * @return A [Sequence] of all [Retrievable]s in the database.
     */
    override fun getConnections(subjectIds: Collection<RetrievableId>, predicates: Collection<String>, objectIds: Collection<RetrievableId>) = sequenceWithTx(this.connection.database)  {
        /* Prepare query based on provided parameters. */
        val query = RelationshipTable.selectAll()
        if (subjectIds.isNotEmpty()) {
            query.andWhere { RelationshipTable.subjectId inList subjectIds }
        }
        if (predicates.isNotEmpty()) {
            query.andWhere { RelationshipTable.predicate inList predicates }
        }
        if (objectIds.isNotEmpty()) {
            query.andWhere { RelationshipTable.objectId inList objectIds }
        }

        /* Execute query and convert to [Relationship] */
        try {
            for (result in query) {
                this.yield(result.toRelationship())
            }
        } catch (e: SQLException) {
            LOGGER.error(e) { "Failed to fetch relationships due to SQL error." }
        }
    }

    /**
     * Counts the number of [Retrievable] stored by the database.
     *
     * @return The number of [Retrievable]s.
     */
    override fun count(): Long = try {
        transaction(this.connection.database)  {
            RetrievableTable.selectAll().count()
        }
    } catch (e: Throwable) {
        LOGGER.error(e) { "Failed to count retrievables due to error." }
        0L
    }
}