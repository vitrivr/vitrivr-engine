package org.vitrivr.engine.database.pgvector.retrievable

import org.vitrivr.engine.core.database.retrievable.RetrievableReader
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.model.retrievable.RetrievableId
import org.vitrivr.engine.core.model.retrievable.Retrieved
import org.vitrivr.engine.database.pgvector.*
import java.util.*

/**
 * A [RetrievableReader] implementation for PostgreSQL with pgVector.
 *
 * @author Ralph Gasser
 * @version 1.0.0
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
            this@RetrievableReader.connection.jdbc.prepareStatement("SELECT * FROM $RETRIEVABLE_ENTITY_NAME WHERE $RETRIEVABLE_ID_COLUMN_NAME = ANY (?)").use { statement ->
                statement.setArray(1,  this@RetrievableReader.connection.jdbc.createArrayOf("uuid", values))
                statement.executeQuery().use { result ->
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
     * Returns all [Retrievable]s stored by the database.
     *
     * @return A [Sequence] of all [Retrievable]s in the database.
     */
    override fun getAll(): Sequence<Retrievable> {
        try {
            this.connection.jdbc.prepareStatement("SELECT * FROM $RETRIEVABLE_ENTITY_NAME").use { stmt ->
                val result = stmt.executeQuery()
                return sequence {
                    while (result.next()) {
                        yield(Retrieved(result.getObject(RETRIEVABLE_ID_COLUMN_NAME, UUID::class.java), result.getString(RETRIEVABLE_TYPE_COLUMN_NAME), false))
                    }
                    result.close()
                }
            }
        } catch (e: Exception) {
            LOGGER.error(e) { "Failed to check for retrievables due to SQL error." }
            return emptySequence()
        }
    }

    override fun getConnections(
        subjectIds: Collection<RetrievableId>,
        predicates: Collection<String>,
        objectIds: Collection<RetrievableId>
    ): Sequence<Triple<RetrievableId, String, RetrievableId>> {
        TODO("Not yet implemented")
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
        } catch (e: Exception) {
            LOGGER.error(e) { "Failed to count retrievable due to SQL error." }
            return 0L
        }
    }
}