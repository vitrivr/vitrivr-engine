package org.vitrivr.engine.base.database.cottontail.descriptors.label

import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import io.grpc.StatusRuntimeException
import org.vitrivr.cottontail.client.language.basics.expression.Column
import org.vitrivr.cottontail.client.language.basics.expression.Literal
import org.vitrivr.cottontail.client.language.basics.predicate.Compare
import org.vitrivr.cottontail.client.language.dml.BatchInsert
import org.vitrivr.cottontail.client.language.dml.Insert
import org.vitrivr.cottontail.client.language.dml.Update
import org.vitrivr.cottontail.core.values.StringValue
import org.vitrivr.engine.base.database.cottontail.CottontailConnection
import org.vitrivr.engine.base.database.cottontail.descriptors.AbstractDescriptorWriter
import org.vitrivr.engine.core.model.descriptor.struct.StructDescriptor
import org.vitrivr.engine.core.model.metamodel.Schema

private val logger: KLogger = KotlinLogging.logger {}

/**
 * An [AbstractDescriptorWriter] for [StructDescriptor]s.
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
class StructDescriptorWriter(field: Schema.Field<*, StructDescriptor>, connection: CottontailConnection) : AbstractDescriptorWriter<StructDescriptor>(field, connection) {

    /**
     * Adds (writes) a single [StructDescriptor] using this [StructDescriptorWriter].
     *
     * @param item The [StructDescriptor] to write.
     * @return True on success, false otherwise.
     */
    override fun add(item: StructDescriptor): Boolean {
        val insert = Insert(this.entityName).values(
            CottontailConnection.DESCRIPTOR_ID_COLUMN_NAME to StringValue(item.id.toString()),
            CottontailConnection.RETRIEVABLE_ID_COLUMN_NAME to StringValue(item.retrievableId.toString()),
        )

        /* Append fields. */
        for ((field, value) in item.values()) {
            insert.any(field, value)
        }

        return try {
            this.connection.client.insert(insert).use {
                it.hasNext()
            }
        } catch (e: StatusRuntimeException) {
            logger.error(e) { "Failed to persist descriptor ${item.id} due to exception." }
            false
        }
    }

    /**
     * Adds (writes) a batch of [StructDescriptor] using this [StructDescriptorWriter].
     *
     * @param items A [Iterable] of [StructDescriptor]s to write.
     * @return True on success, false otherwise.
     */
    override fun addAll(items: Iterable<StructDescriptor>): Boolean {
        /* Prepare insert query. */
        var size = 0
        val insert = BatchInsert(this.entityName).columns(CottontailConnection.DESCRIPTOR_ID_COLUMN_NAME, CottontailConnection.RETRIEVABLE_ID_COLUMN_NAME)
        for (item in items) {
            size += 1
            val value = item.values()
            val inserts: MutableList<Any?> = mutableListOf(
                item.id.toString(),
                item.retrievableId.toString()
            )
            item.schema().forEach { inserts.add(value[it.name]) }
            insert.any(*inserts.toTypedArray())
        }

        /* Insert values. */
        return try {
            this.connection.client.insert(insert).use {
                it.hasNext()
            }
        } catch (e: StatusRuntimeException) {
            logger.error(e) { "Failed to persist $size scalar descriptors due to exception." }
            false
        }
    }

    /**
     * Updates a specific [StructDescriptor] using this [StructDescriptorWriter].
     *
     * @param item A [StructDescriptor]s to update.
     * @return True on success, false otherwise.
     */
    override fun update(item: StructDescriptor): Boolean {
        val update = Update(this.entityName).where(
            Compare(
                Column(this.entityName.column(CottontailConnection.DESCRIPTOR_ID_COLUMN_NAME)),
                Compare.Operator.EQUAL,
                Literal(item.id.toString())
            )
        )

        /* Append values. */
        for ((field, value) in item.values()) {
            update.any(field to value)
        }

        /* Update values. */
        return try {
            this.connection.client.update(update)
            true
        } catch (e: StatusRuntimeException) {
            logger.error(e) { "Failed to update descriptor due to exception." }
            false
        }
    }
}