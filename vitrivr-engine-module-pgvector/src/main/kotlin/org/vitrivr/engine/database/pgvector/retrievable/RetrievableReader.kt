package org.vitrivr.engine.database.pgvector.retrievable

import org.vitrivr.engine.core.database.retrievable.RetrievableReader
import org.vitrivr.engine.core.model.relationship.Relationship
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.model.retrievable.RetrievableId
import org.vitrivr.engine.core.model.retrievable.Retrieved
import org.vitrivr.engine.database.pgvector.*
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
    override fun get(id: RetrievableId): Retrievable? {
        try {
            this.connection.jdbc.prepareStatement("SELECT * FROM $RETRIEVABLE_ENTITY_NAME WHERE $RETRIEVABLE_ID_COLUMN_NAME = ?").use { stmt ->
                stmt.setObject(1, id)
                stmt.executeQuery().use { res ->
                    if (res.next() ) {
                        return Retrieved(res.getObject(RETRIEVABLE_ID_COLUMN_NAME, UUID::class.java), res.getString(RETRIEVABLE_TYPE_COLUMN_NAME), false)
                    } else {
                        return null
                    }
                }
            }
        } catch (e: Exception) {
            LOGGER.error(e) { "Failed to check for retrievable $id due to SQL error." }
            return null
        }
    }

    /**
     * Checks whether a [Retrievable] with the provided [RetrievableId] exists.
     *
     * @param id The [RetrievableId], i.e., the [UUID] of the [Retrievable] to check for.
     * @return True if descriptor exists, false otherwise
     */
    override fun exists(id: RetrievableId): Boolean {
        try {
            this.connection.jdbc.prepareStatement("SELECT count(*) FROM $RETRIEVABLE_ENTITY_NAME WHERE $RETRIEVABLE_ID_COLUMN_NAME = ?").use { stmt ->
                stmt.setObject(1, id)
                stmt.executeQuery().use { res ->
                    res.next()
                    return res.getLong(1) > 0L
                }
            }
        } catch (e: Exception) {
            LOGGER.error(e) { "Failed to check for retrievable $id due to SQL error." }
            return false
        }
    }

    /**
     * Returns all [Retrievable]s that match any of the provided [RetrievableId]
     *
     * @param ids A [Iterable] of [RetrievableId]s to return.
     * @return A [Sequence] of all [Retrievable].
     */
    override fun getAll(ids: Iterable<RetrievableId>): Sequence<Retrievable> = sequence {
        try {
            val values = ids.map { it }.toTypedArray()
            this@RetrievableReader.connection.jdbc.prepareStatement("WITH x(ids) AS ( VALUES (?::uuid[])) SELECT ${RETRIEVABLE_ENTITY_NAME}.* FROM $RETRIEVABLE_ENTITY_NAME, x WHERE $RETRIEVABLE_ID_COLUMN_NAME = ANY (x.ids) ORDER BY array_position(x.ids, ${RETRIEVABLE_ID_COLUMN_NAME})").use { statement ->
                statement.setArray(1,  this@RetrievableReader.connection.jdbc.createArrayOf("uuid", values))
                statement.executeQuery().use { result ->
                    while (result.next()) {
                        yield(Retrieved(result.getObject(RETRIEVABLE_ID_COLUMN_NAME, UUID::class.java), result.getString(RETRIEVABLE_TYPE_COLUMN_NAME), false))
                    }
                }
            }
        } catch (e: Exception) {
            LOGGER.error(e) { "Failed to fetch retrievables due to SQL error." }
        }
    }

    /**
     * Returns all [Retrievable]s stored by the database.
     *
     * @return A [Sequence] of all [Retrievable]s in the database.
     */
    override fun getAll(): Sequence<Retrievable> = sequence {
        try {
            this@RetrievableReader.connection.jdbc.prepareStatement("SELECT * FROM $RETRIEVABLE_ENTITY_NAME").use { stmt ->
                stmt.executeQuery().use { result ->
                    while (result.next()) {
                        yield(Retrieved(result.getObject(RETRIEVABLE_ID_COLUMN_NAME, UUID::class.java), result.getString(RETRIEVABLE_TYPE_COLUMN_NAME), false))
                    }
                }
            }
        } catch (e: Exception) {
            LOGGER.error(e) { "Failed to check for retrievables due to SQL error." }
        }
    }

    /**
     * Returns all relationships thar are stored by the database and that match the given query.
     *
     * @return A [Sequence] of all [Retrievable]s in the database.
     */
    override fun getConnections(subjectIds: Collection<RetrievableId>, predicates: Collection<String>, objectIds: Collection<RetrievableId>): Sequence<Relationship.ById> {
        val query = StringBuilder("SELECT * FROM \"$RELATIONSHIP_ENTITY_NAME\" WHERE ")
        if (subjectIds.isNotEmpty()) {
            query.append("$SUBJECT_ID_COLUMN_NAME = ANY (?)")
        }
        if (predicates.isNotEmpty()) {
            if (subjectIds.isNotEmpty()) {
                query.append(" AND ")
            }
            query.append("$PREDICATE_COLUMN_NAME = ANY (?)")
        }
        if (objectIds.isNotEmpty()) {
            if (subjectIds.isNotEmpty() || predicates.isNotEmpty()) {
                query.append(" AND ")
            }
            query.append("$OBJECT_ID_COLUMN_NAME = ANY (?)")
        }
        if (query.endsWith("WHERE ")) {
            query.delete(query.length - 7, query.length)
        }

        return sequence {
            try {
                this@RetrievableReader.connection.jdbc.prepareStatement(query.toString()).use { stmt ->
                    var index = 1
                    if (subjectIds.isNotEmpty()) {
                        stmt.setArray(index++, this@RetrievableReader.connection.jdbc.createArrayOf("uuid", subjectIds.toTypedArray()))
                    }
                    if (predicates.isNotEmpty()) {
                        stmt.setArray(index++, this@RetrievableReader.connection.jdbc.createArrayOf("varchar", predicates.toTypedArray()))
                    }
                    if (objectIds.isNotEmpty()) {
                        stmt.setArray(index, this@RetrievableReader.connection.jdbc.createArrayOf("uuid", objectIds.toTypedArray()))
                    }
                    stmt.executeQuery().use { result ->
                        while (result.next()) {
                            yield(Relationship.ById(result.getObject(SUBJECT_ID_COLUMN_NAME, UUID::class.java), result.getString(PREDICATE_COLUMN_NAME), result.getObject(OBJECT_ID_COLUMN_NAME, UUID::class.java), false))
                        }
                    }
                }
            } catch (e: SQLException) {
                LOGGER.error(e) { "Failed to fetch relationships due to SQL error." }
            }
        }

    }

    /**
     * Counts the number of [Retrievable] stored by the database.
     *
     * @return The number of [Retrievable]s.
     */
    override fun count(): Long {
        try {
            this.connection.jdbc.prepareStatement("SELECT COUNT(*) FROM $RETRIEVABLE_ENTITY_NAME;").use { stmt ->
                stmt.executeQuery().use { result ->
                    result.next()
                    return result.getLong(1)
                }
            }
        } catch (e: SQLException) {
            LOGGER.error(e) { "Failed to count retrievable due to SQL error." }
            return 0L
        }
    }
}