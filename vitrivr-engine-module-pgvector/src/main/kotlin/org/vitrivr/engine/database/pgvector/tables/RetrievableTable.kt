package org.vitrivr.engine.database.pgvector.tables

import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.ResultRow
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.model.retrievable.Retrieved

/**
 * Table definition for the [Retrievable] entity.
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
object RetrievableTable: UUIDTable("retrievable", "retrievableid") {
    val type = varchar("type", 255).nullable()

    /**
     * Converts a [ResultRow] to a [Retrieved] object.
     *
     * @return The [Retrieved] object.
     */
    fun ResultRow.toRetrieved() = Retrieved(this[id].value, this[type], false)
}