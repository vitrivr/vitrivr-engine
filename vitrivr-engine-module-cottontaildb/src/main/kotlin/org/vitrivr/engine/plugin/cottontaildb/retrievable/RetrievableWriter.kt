package org.vitrivr.engine.plugin.cottontaildb.retrievable

import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import io.grpc.StatusRuntimeException
import org.vitrivr.cottontail.client.language.basics.expression.Column
import org.vitrivr.cottontail.client.language.basics.expression.Literal
import org.vitrivr.cottontail.client.language.basics.predicate.And
import org.vitrivr.cottontail.client.language.basics.predicate.Compare
import org.vitrivr.cottontail.client.language.dml.BatchInsert
import org.vitrivr.cottontail.client.language.dml.Delete
import org.vitrivr.cottontail.client.language.dml.Insert
import org.vitrivr.cottontail.client.language.dml.Update
import org.vitrivr.cottontail.core.database.Name
import org.vitrivr.cottontail.core.values.StringValue
import org.vitrivr.cottontail.core.values.UuidValue
import org.vitrivr.engine.core.database.retrievable.RetrievableWriter
import org.vitrivr.engine.core.model.relationship.Relationship
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.plugin.cottontaildb.*

private val logger: KLogger = KotlinLogging.logger {}


/**
 * A [RetrievableWriter] implementation for Cottontail DB.
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
internal class RetrievableWriter(private val connection: CottontailConnection) : RetrievableWriter {

    /** The [Name.EntityName] of the retrievable entity. */
    private val entityName: Name.EntityName = Name.EntityName.create(this.connection.schemaName, RETRIEVABLE_ENTITY_NAME)

    /** The [Name.EntityName] of the relationship entity. */
    private val relationshipEntityName: Name.EntityName = Name.EntityName.create(this.connection.schemaName, RELATIONSHIP_ENTITY_NAME)

    /**
     * Adds a new [Retrievable] to the database using this [RetrievableWriter] instance.
     *
     * @param item [Retrievable] to add.
     */
    override fun add(item: Retrievable): Boolean {
        val insert = Insert(this.entityName)
            .value(RETRIEVABLE_ID_COLUMN_NAME, UuidValue(item.id))
            .value(RETRIEVABLE_TYPE_COLUMN_NAME, item.type?.let { StringValue(it) })
        return try {
            return this.connection.client.insert(insert).use {
                it.hasNext()
            }
        } catch (e: StatusRuntimeException) {
            logger.error(e) { "Failed to persist retrievable ${item.id} due to exception." }
            false
        }
    }

    /**
     * Adds new [Retrievable]s to the database using this [RetrievableWriter] instance.
     *
     * @param items An [Iterable] of [Retrievable]s to add.
     */
    override fun addAll(items: Iterable<Retrievable>): Boolean {
        /* Prepare insert query. */
        var size = 0
        val insert = BatchInsert(this.entityName).columns(RETRIEVABLE_ID_COLUMN_NAME, RETRIEVABLE_TYPE_COLUMN_NAME)
        for (item in items) {
            size += 1
            insert.values(UuidValue(item.id), item.type?.let { StringValue(it) })
        }

        /* Insert values. */
        return try {
            return this.connection.client.insert(insert).use {
                it.hasNext()
            }
        } catch (e: StatusRuntimeException) {
            logger.error(e) { "Failed to persist $size retrievables due to exception." }
            false
        }
    }

    /**
     * Updates a specific [Retrievable] using this [RetrievableWriter].
     *
     * @param item A [Retrievable]s to update.
     * @return True on success, false otherwise.
     */
    override fun update(item: Retrievable): Boolean {
        val update = Update(this.entityName).where(
            Compare(
                Column(this.entityName.column(RETRIEVABLE_ID_COLUMN_NAME)),
                Compare.Operator.EQUAL,
                Literal(UuidValue(item.id))
            )
        ).values(RETRIEVABLE_TYPE_COLUMN_NAME to item.type?.let { StringValue(it) })

        /* Update values. */
        return try {
            this.connection.client.update(update)
            true
        } catch (e: StatusRuntimeException) {
            logger.error(e) { "Failed to update descriptor due to exception." }
            false
        }
    }

    /**
     * Deletes (writes) a [Retrievable] using this [RetrievableWriter].
     *
     * @param item A [Retrievable]s to delete.
     * @return True on success, false otherwise.
     */
    override fun delete(item: Retrievable): Boolean {
        val delete = Delete(this.entityName).where(
            Compare(
                Column(this.entityName.column(RETRIEVABLE_ID_COLUMN_NAME)),
                Compare.Operator.EQUAL,
                Literal(UuidValue(item.id))
            )
        )

        /* Delete values. */
        return try {
            this.connection.client.delete(delete)
            true
        } catch (e: StatusRuntimeException) {
            logger.error(e) { "Failed to delete retrievable due to exception." }
            false
        }
    }

    /**
     * Deletes (writes) a iterable of [Retrievable]s using this [RetrievableWriter].
     *
     * @param items A [Iterable] of [Retrievable]s to delete.
     * @return True on success, false otherwise.
     */
    override fun deleteAll(items: Iterable<Retrievable>): Boolean {
        val ids = items.map { UuidValue(it.id) }
        val delete = Delete(this.entityName).where(
            Compare(
                Column(this.entityName.column(RETRIEVABLE_ID_COLUMN_NAME)),
                Compare.Operator.IN,
                org.vitrivr.cottontail.client.language.basics.expression.List(ids.toTypedArray())
            )
        )

        /* Delete values. */
        return try {
            this.connection.client.delete(delete)
            true
        } catch (e: StatusRuntimeException) {
            logger.error(e) { "Failed to delete retrievables due to exception." }
            false
        }
    }

    /**
     * Persists a [Relationship].
     *
     * @param relationship [Relationship] to persist
     * @return True on success, false otherwise.
     */
    override fun connect(relationship: Relationship): Boolean {
        val insert = Insert(this.relationshipEntityName)
            .value(SUBJECT_ID_COLUMN_NAME, UuidValue(relationship.subjectId))
            .value(PREDICATE_COLUMN_NAME, StringValue(relationship.predicate))
            .value(OBJECT_ID_COLUMN_NAME, UuidValue(relationship.objectId))

        /* Insert values. */
        return try {
            this.connection.client.insert(insert)
            true
        } catch (e: StatusRuntimeException) {
            logger.error(e) { "Failed to establish connection due to exception." }
            false
        }
    }

    /**
     * Persists an [Iterable] of [Relationship]s.
     *
     * @param relationships An [Iterable] of [Relationship]s to persist.
     * @return True on success, false otherwise.
     */
    override fun connectAll(relationships: Iterable<Relationship>): Boolean {
        var size = 0
        val insert = BatchInsert(this.relationshipEntityName).columns(SUBJECT_ID_COLUMN_NAME, PREDICATE_COLUMN_NAME, OBJECT_ID_COLUMN_NAME)
        relationships.forEach { r ->
            size += 1
            insert.values(UuidValue(r.subjectId), StringValue(r.predicate), UuidValue(r.objectId))
        }

        return try {
            this.connection.client.insert(insert).use { it.hasNext() }
            true
        } catch (e: StatusRuntimeException) {
            logger.error(e) { "Failed to establish connection due to exception." }
            false
        }
    }


    /**
     * Severs the specified connection between two [Retrievable]s.
     *
     * @param relationship [Relationship] to delete
     * @return True on success, false otherwise.
     */
    override fun disconnect(relationship: Relationship): Boolean {
        val delete = Delete(this.relationshipEntityName).where(
            And(
                Compare(Column(PREDICATE_COLUMN_NAME), Compare.Operator.EQUAL, Literal(StringValue(relationship.predicate))),
                And(
                    Compare(Column(OBJECT_ID_COLUMN_NAME), Compare.Operator.EQUAL, Literal(UuidValue(relationship.objectId))),
                    Compare(Column(SUBJECT_ID_COLUMN_NAME), Compare.Operator.EQUAL, Literal(UuidValue(relationship.subjectId)))
                )
            )
        )

        /* Insert values. */
        return try {
            this.connection.client.delete(delete)
            true
        } catch (e: StatusRuntimeException) {
            logger.error(e) { "Failed to sever connection due to exception." }
            false
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