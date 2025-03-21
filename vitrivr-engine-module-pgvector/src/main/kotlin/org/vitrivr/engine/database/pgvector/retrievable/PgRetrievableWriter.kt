package org.vitrivr.engine.database.pgvector.retrievable

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.inList
import org.jetbrains.exposed.sql.transactions.transaction
import org.vitrivr.engine.core.database.retrievable.RetrievableWriter
import org.vitrivr.engine.core.model.relationship.Relationship
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.database.pgvector.*
import org.vitrivr.engine.database.pgvector.tables.RelationshipTable
import org.vitrivr.engine.database.pgvector.tables.RelationshipTable.objectId
import org.vitrivr.engine.database.pgvector.tables.RelationshipTable.predicate
import org.vitrivr.engine.database.pgvector.tables.RelationshipTable.subjectId
import org.vitrivr.engine.database.pgvector.tables.RetrievableTable

/**
 * A [RetrievableWriter] implementation for PostgreSQL with pgVector.
 *
 * @author Ralph Gasser
 * @version 1.1.0
 */
internal class PgRetrievableWriter(override val connection: PgVectorConnection) : RetrievableWriter {
    /**
     * Adds a new [Retrievable] to the database using this [RetrievableWriter] instance.
     *
     * @param item [Retrievable] to add.
     */
    override fun add(item: Retrievable): Boolean =  try {
        transaction(this.connection.database)  {
            RetrievableTable.insert {
                it[id] = item.id
                it[type] = item.type
            }
        }
        true
    } catch (e: Throwable) {
        LOGGER.error(e) { "Failed to persist retrievable ${item.id} due to SQL error." }
        false
    }

    /**
     * Adds new [Retrievable]s to the database using this [RetrievableWriter] instance.
     *
     * @param items An [Iterable] of [Retrievable]s to add.
     */
    override fun addAll(items: Iterable<Retrievable>): Boolean = try {
        transaction(this.connection.database)  {
            RetrievableTable.batchInsert(items) { item ->
                this[RetrievableTable.id] = item.id
                this[RetrievableTable.type] = item.type
            }
        }
        true
    } catch (e: Throwable) {
        LOGGER.error(e) { "Failed to persist retrievables due to error." }
        false
    }

    /**
     * Updates a specific [Retrievable] using this [RetrievableWriter].
     *
     * @param item A [Retrievable]s to update.
     * @return True on success, false otherwise.
     */
    override fun update(item: Retrievable): Boolean = try {
        transaction(this.connection.database)  {
            RetrievableTable.update({ RetrievableTable.id eq item.id}) {
                it[type] = item.type
            } > 0
        }
    } catch (e: Throwable) {
        LOGGER.error(e) { "Failed to update retrievable ${item.id} due to error." }
        false
    }

    /**
     * Deletes (writes) a [Retrievable] using this [RetrievableWriter].
     *
     * @param item A [Retrievable]s to delete.
     * @return True on success, false otherwise.
     */
    override fun delete(item: Retrievable): Boolean = try {
        transaction(this.connection.database)  {
            RetrievableTable.deleteWhere { RetrievableTable.id eq item.id } > 0
        }
    } catch (e: Throwable) {
        LOGGER.error(e) {  "Failed to delete retrievable ${item.id} due to error." }
        false
    }

    /**
     * Deletes (writes) a iterable of [Retrievable]s using this [RetrievableWriter].
     *
     * @param items A [Iterable] of [Retrievable]s to delete.
     * @return True on success, false otherwise.
     */
    override fun deleteAll(items: Iterable<Retrievable>) = try {
        transaction(this.connection.database)  {
            RetrievableTable.deleteWhere { RetrievableTable.id inList items.map { it.id } } > 0
        }
    } catch (e: Throwable) {
        LOGGER.error(e) {  "Failed to delete retrievables due to error." }
        false
    }

    /**
     * Persists a [Relationship].
     *
     * @param relationship [Relationship] to persist
     * @return True on success, false otherwise.
     */
    override fun connect(relationship: Relationship): Boolean = try {
        transaction(this.connection.database)  {
            RelationshipTable.insert {
                it[subjectId] = relationship.subjectId
                it[predicate] = relationship.predicate
                it[objectId] = relationship.objectId
            }
        }
        true
    } catch (e: Throwable) {
        LOGGER.error(e) { "Failed to insert relationship ${relationship.subjectId} -(${relationship.predicate}-> ${relationship.objectId} due to error." }
        false
    }

    /**
     * Persists an [Iterable] of [Relationship]s.
     *
     * @param relationships An [Iterable] of [Relationship]s to persist.
     * @return True on success, false otherwise.
     */
    override fun connectAll(relationships: Iterable<Relationship>): Boolean = try {
        transaction(this.connection.database)  {
            RelationshipTable.batchInsert(relationships) { r ->
                this[subjectId] = r.subjectId
                this[predicate] = r.predicate
                this[objectId] = r.objectId
            }
        }
        true
    } catch (e: Throwable) {
        LOGGER.error(e) { "Failed to insert relationships due to SQL error." }
        false
    }

    /**
     * Severs the specified connection between two [Retrievable]s.
     *
     * @param relationship [Relationship] to delete
     * @return True on success, false otherwise.
     */
    override fun disconnect(relationship: Relationship): Boolean = try {
        transaction(this.connection.database)  {
            RelationshipTable.deleteWhere {
                (subjectId eq relationship.subjectId) and (predicate eq relationship.predicate) and (objectId eq relationship.objectId)
            } > 0
        }
    } catch (e: Throwable) {
        LOGGER.error(e) { "Failed to delete relationship ${relationship.subjectId} -(${relationship.predicate}-> ${relationship.objectId} due to SQL error." }
        false
    }

    /**
     * Deletes all [Relationship]s
     *
     * @param relationships An [Iterable] of [Relationship] to delete.
     * @return True on success, false otherwise.
     */
    override fun disconnectAll(relationships: Iterable<Relationship>): Boolean = transaction(this.connection.database)  {
        relationships.map { this@PgRetrievableWriter.disconnect(it) }.all { it }
    }
}