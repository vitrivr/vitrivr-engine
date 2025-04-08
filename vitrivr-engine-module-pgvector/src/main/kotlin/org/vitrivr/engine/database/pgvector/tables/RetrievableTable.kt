package org.vitrivr.engine.database.pgvector.tables

import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.ResultRow
import org.vitrivr.engine.core.model.descriptor.Descriptor
import org.vitrivr.engine.core.model.relationship.Relationship
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.model.retrievable.Retrieved
import org.vitrivr.engine.core.model.retrievable.attributes.RetrievableAttribute
import org.vitrivr.engine.database.pgvector.RETRIEVABLE_ENTITY_NAME
import org.vitrivr.engine.database.pgvector.RETRIEVABLE_ID_COLUMN_NAME
import org.vitrivr.engine.database.pgvector.RETRIEVABLE_TYPE_COLUMN_NAME

/**
 * Table definition for the [Retrievable] entity.
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
object RetrievableTable: UUIDTable(RETRIEVABLE_ENTITY_NAME, RETRIEVABLE_ID_COLUMN_NAME) {
    val type = varchar(RETRIEVABLE_TYPE_COLUMN_NAME, 255)

    /**
     * Converts a [ResultRow] to a [Retrieved] object.
     *
     * @param descriptors The [Descriptor]s to append.
     * @param attributes The [RetrievableAttribute]s to append.
     * @param relationships The [Relationship]s to append
     * @return The [Retrieved] object.
     */
    fun ResultRow.toRetrieved(
        descriptors: Set<Descriptor<*>> = emptySet(),
        attributes: Set<RetrievableAttribute> = emptySet(),
        relationships: Set<Relationship> = emptySet()
    ) = Retrieved(this[id].value, this[type], emptyList(), descriptors, attributes, relationships, false)
}