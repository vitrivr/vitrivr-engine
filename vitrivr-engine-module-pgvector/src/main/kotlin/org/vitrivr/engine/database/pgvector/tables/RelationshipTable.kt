package org.vitrivr.engine.database.pgvector.tables

import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.Table
import org.vitrivr.engine.core.model.relationship.Relationship
import org.vitrivr.engine.core.model.retrievable.Retrievable

/**
 * Table object for the [Relationship] entity.
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
object RelationshipTable: Table("relationships") {

    /** The reference to th [Retrievable] in subject position of the [Relationship]. */
    val subjectId = reference("subjectId", RetrievableTable, onDelete = ReferenceOption.CASCADE).index()

    /** The predicate of the [Relationship]. */
    val predicate = varchar("predicate", 255).index()

    /** The reference to th [Retrievable] in object position of the [Relationship]. */
    val objectId = reference("objectId", RetrievableTable, onDelete = ReferenceOption.CASCADE).index()

    /** The [Retrievable] in object position of the [Relationship]. */
    override val primaryKey = PrimaryKey(this.subjectId, this.predicate, this.objectId)

    /**
     * Converts a [ResultRow] to a [Relationship.ById] object.
     *
     * @return The [Relationship.ById] object.
     */
    fun ResultRow.toRelationship() = Relationship.ById(this[subjectId].value, this[predicate], this[objectId].value, false)
}