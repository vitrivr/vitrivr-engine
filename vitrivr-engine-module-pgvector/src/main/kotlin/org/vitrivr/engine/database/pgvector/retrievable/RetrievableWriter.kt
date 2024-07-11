package org.vitrivr.engine.database.pgvector.retrievable

import org.vitrivr.engine.core.database.retrievable.RetrievableWriter
import org.vitrivr.engine.core.model.relationship.Relationship
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.database.pgvector.*
import java.sql.SQLException

/**
 * A [RetrievableWriter] implementation for PostgreSQL with pgVector.
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
internal class RetrievableWriter(override val connection: PgVectorConnection): RetrievableWriter {
    /**
     * Adds a new [Retrievable] to the database using this [RetrievableWriter] instance.
     *
     * @param item [Retrievable] to add.
     */
    override fun add(item: Retrievable): Boolean {
        try {
            this.connection.jdbc.prepareStatement("INSERT INTO $RETRIEVABLE_ENTITY_NAME ($RETRIEVABLE_ID_COLUMN_NAME, $RETRIEVABLE_TYPE_COLUMN_NAME) VALUES (?, ?);").use { stmt ->
                stmt.setObject(1, item.id)
                stmt.setString(2, item.type)
                return stmt.executeUpdate() == 1
            }
        } catch (e: SQLException) {
            LOGGER.error(e) { "Failed to persist retrievable ${item.id} due to SQL error." }
            return false
        }
    }

    /**
     * Adds new [Retrievable]s to the database using this [RetrievableWriter] instance.
     *
     * @param items An [Iterable] of [Retrievable]s to add.
     */
    override fun addAll(items: Iterable<Retrievable>): Boolean {
        try {
            this.connection.jdbc.prepareStatement("INSERT INTO $RETRIEVABLE_ENTITY_NAME ($RETRIEVABLE_ID_COLUMN_NAME, $RETRIEVABLE_TYPE_COLUMN_NAME) VALUES (?, ?);").use { stmt ->
                for (item in items) {
                    stmt.setObject(1, item.id)
                    stmt.setString(2, item.type)
                    stmt.addBatch()
                }
                return stmt.executeBatch().all { it == 1 }
            }
        } catch (e: SQLException) {
            LOGGER.error(e) { "Failed to persist retrievables due to SQL error." }
            return false
        }
    }

    /**
     * Updates a specific [Retrievable] using this [RetrievableWriter].
     *
     * @param item A [Retrievable]s to update.
     * @return True on success, false otherwise.
     */
    override fun update(item: Retrievable): Boolean {
        try {
            this.connection.jdbc.prepareStatement("UPDATE $RETRIEVABLE_ENTITY_NAME SET $RETRIEVABLE_TYPE_COLUMN_NAME = ? WHERE $RETRIEVABLE_ID_COLUMN_NAME = ?").use { stmt ->
                stmt.setString(1, item.type)
                stmt.setObject(2, item.id)
                return stmt.execute()
            }
        } catch (e: SQLException) {
            LOGGER.error(e) { "Failed to update retrievable ${item.id} due to SQL error." }
            return false
        }
    }

    /**
     * Deletes (writes) a [Retrievable] using this [RetrievableWriter].
     *
     * @param item A [Retrievable]s to delete.
     * @return True on success, false otherwise.
     */
    override fun delete(item: Retrievable): Boolean {
        try {
            this.connection.jdbc.prepareStatement("DELETE FROM $RETRIEVABLE_ENTITY_NAME WHERE $RETRIEVABLE_ID_COLUMN_NAME = ?;").use { stmt ->
                stmt.setObject(1, item.id)
                return stmt.executeUpdate() > 0
            }
        } catch (e: SQLException) {
            LOGGER.error(e) {  "Failed to delete retrievable ${item.id} due to SQL error." }
            return false
        }
    }

    /**
     * Deletes (writes) a iterable of [Retrievable]s using this [RetrievableWriter].
     *
     * @param items A [Iterable] of [Retrievable]s to delete.
     * @return True on success, false otherwise.
     */
    override fun deleteAll(items: Iterable<Retrievable>): Boolean {
        try {
            this.connection.jdbc.prepareStatement("DELETE FROM $RETRIEVABLE_ENTITY_NAME WHERE $RETRIEVABLE_ID_COLUMN_NAME = ANY (?);").use { stmt ->
                val values = items.map { it.id }.toTypedArray()
                stmt.setArray(1, this.connection.jdbc.createArrayOf("uuid", values))
                return stmt.executeUpdate() > 0
            }
        } catch (e: SQLException) {
            LOGGER.error(e) { "Failed to delete retrievable due to SQL error." }
            return false
        }
    }

    /**
     * Persists a [Relationship].
     *
     * @param relationship [Relationship] to persist
     * @return True on success, false otherwise.
     */
    override fun connect(relationship: Relationship): Boolean {
        try {
            this.connection.jdbc.prepareStatement("INSERT INTO $RELATIONSHIP_ENTITY_NAME ($OBJECT_ID_COLUMN_NAME,$PREDICATE_COLUMN_NAME,$SUBJECT_ID_COLUMN_NAME) VALUES (?,?,?)").use { stmt ->
                stmt.setObject(1, relationship.objectId)
                stmt.setString(2, relationship.predicate)
                stmt.setObject(3, relationship.subjectId)
                return stmt.execute()
            }
        } catch (e: SQLException) {
            LOGGER.error(e) { "Failed to insert relationship ${relationship.objectId} -(${relationship.predicate}-> ${relationship.subjectId} due to SQL error." }
            return false
        }
    }

    /**
     * Persists an [Iterable] of [Relationship]s.
     *
     * @param relationships An [Iterable] of [Relationship]s to persist.
     * @return True on success, false otherwise.
     */
    override fun connectAll(relationships: Iterable<Relationship>): Boolean {
        try {
            this.connection.jdbc.prepareStatement("INSERT INTO $RELATIONSHIP_ENTITY_NAME ($OBJECT_ID_COLUMN_NAME,$PREDICATE_COLUMN_NAME,$SUBJECT_ID_COLUMN_NAME) VALUES (?,?,?)").use { stmt ->
                for (relationship in relationships) {
                    stmt.setObject(1, relationship.objectId)
                    stmt.setString(2, relationship.predicate)
                    stmt.setObject(3, relationship.subjectId)
                    stmt.addBatch()
                }
                return stmt.executeBatch().all { it == 1 }
            }
        } catch (e: SQLException) {
            LOGGER.error(e) { "Failed to insert relationships due to SQL error." }
            return false
        }
    }

    /**
     * Severs the specified connection between two [Retrievable]s.
     *
     * @param relationship [Relationship] to delete
     * @return True on success, false otherwise.
     */
    override fun disconnect(relationship: Relationship): Boolean {
        try {
            this.connection.jdbc.prepareStatement("DELETE FROM $RELATIONSHIP_ENTITY_NAME WHERE $OBJECT_ID_COLUMN_NAME = ? AND $PREDICATE_COLUMN_NAME = ? AND $SUBJECT_ID_COLUMN_NAME = ?").use { stmt ->
                stmt.setObject(1, relationship.objectId)
                stmt.setString(2, relationship.predicate)
                stmt.setObject(3, relationship.subjectId)
                return stmt.execute()
            }
        } catch (e: SQLException) {
            LOGGER.error(e) { "Failed to delete relationship ${relationship.objectId} -(${relationship.predicate}-> ${relationship.subjectId} due to SQL error." }
            return false
        }
    }

    /**
     * Deletes all [Relationship]s
     *
     * @param relationships An [Iterable] of [Relationship] to delete.
     * @return True on success, false otherwise.
     */
    override fun disconnectAll(relationships: Iterable<Relationship>): Boolean = relationships.map { this.disconnect(it) }.all { it }
}