package org.vitrivr.engine.base.database.cottontail.retrievable

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
import org.vitrivr.cottontail.core.database.Name
import org.vitrivr.cottontail.core.values.StringValue
import org.vitrivr.cottontail.core.values.UuidValue
import org.vitrivr.engine.base.database.cottontail.CottontailConnection
import org.vitrivr.engine.base.database.cottontail.CottontailConnection.Companion.OBJECT_ID_COLUMN_NAME
import org.vitrivr.engine.base.database.cottontail.CottontailConnection.Companion.PREDICATE_COLUMN_NAME
import org.vitrivr.engine.base.database.cottontail.CottontailConnection.Companion.RETRIEVABLE_ID_COLUMN_NAME
import org.vitrivr.engine.base.database.cottontail.CottontailConnection.Companion.RETRIEVABLE_TYPE_COLUMN_NAME
import org.vitrivr.engine.base.database.cottontail.CottontailConnection.Companion.SUBJECT_ID_COLUMN_NAME
import org.vitrivr.engine.core.database.retrievable.RetrievableWriter
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.model.retrievable.RetrievableId

private val logger: KLogger = KotlinLogging.logger {}


/**
 * A [RetrievableWriter] implementation for Cottontail DB.
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
internal class RetrievableWriter(private val connection: CottontailConnection) : RetrievableWriter {

    /** The [Name.EntityName] of the retrievable entity. */
    private val entityName: Name.EntityName = Name.EntityName.create(this.connection.schemaName, CottontailConnection.RETRIEVABLE_ENTITY_NAME)

    /** The [Name.EntityName] of the relationship entity. */
    private val relationshipEntityName: Name.EntityName = Name.EntityName.create(this.connection.schemaName, CottontailConnection.RELATIONSHIP_ENTITY_NAME)

    /**
     * Adds a new [Retrievable] to the database using this [RetrievableWriter] instance.
     *
     * @param item [Retrievable] to add.
     */
    override fun add(item: Retrievable): Boolean {
        val insert = Insert(this.entityName)
            .value(RETRIEVABLE_ID_COLUMN_NAME, UuidValue(item.id))
            .any(RETRIEVABLE_TYPE_COLUMN_NAME, item.type?.let { StringValue(it) })
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
            insert.any(item.id, item.type)
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
     *
     */
    override fun update(item: Retrievable): Boolean {
        TODO("Not yet implemented")
    }

    /**
     * Connects two [Retrievable] (specified by their [RetrievableId]) through a subject predicate, object relationship.
     *
     * @param subject [RetrievableId] of the subject [Retrievable]
     * @param predicate The predicate describing the relationship.
     * @param object [RetrievableId] of the object [Retrievable]
     * @return True on success, false otherwise.
     */
    override fun connect(subject: RetrievableId, predicate: String, `object`: RetrievableId): Boolean {
        val insert = Insert(this.relationshipEntityName)
            .value(SUBJECT_ID_COLUMN_NAME, UuidValue(subject))
            .value(PREDICATE_COLUMN_NAME, StringValue(predicate))
            .value(OBJECT_ID_COLUMN_NAME, UuidValue(`object`))

        /* Insert values. */
        return try {
            this.connection.client.insert(insert)
            true
        } catch (e: StatusRuntimeException) {
            logger.error(e) { "Failed to establish connection due to exception." }
            false
        }
    }

    override fun connectAll(subjects: Iterable<RetrievableId>, predicate: String, objects: Iterable<RetrievableId>): Boolean {
        var size = 0
        val insert = BatchInsert(this.relationshipEntityName).columns(SUBJECT_ID_COLUMN_NAME, PREDICATE_COLUMN_NAME, OBJECT_ID_COLUMN_NAME)
        subjects.zip(objects).forEach { (subject, obj) ->
            size += 1
            insert.values(UuidValue(subject), StringValue(predicate), UuidValue(obj))
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
     * @param subject [RetrievableId] of the subject [Retrievable].
     * @param predicate The predicate describing the relationship.
     * @param object [RetrievableId] of the object [Retrievable].
     * @return True on success, false otherwise.
     */
    override fun disconnect(subject: RetrievableId, predicate: String, `object`: RetrievableId): Boolean {
        val delete = Delete(this.relationshipEntityName).where(
            And(
                Compare(Column(PREDICATE_COLUMN_NAME), Compare.Operator.EQUAL, Literal(StringValue(predicate))),
                And(
                    Compare(Column(OBJECT_ID_COLUMN_NAME), Compare.Operator.EQUAL, Literal(UuidValue(`object`))),
                    Compare(Column(SUBJECT_ID_COLUMN_NAME), Compare.Operator.EQUAL, Literal(UuidValue(subject)))
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
}