package org.vitrivr.engine.plugin.cottontaildb.descriptors.string

import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import io.grpc.StatusRuntimeException
import org.vitrivr.cottontail.client.language.basics.expression.Column
import org.vitrivr.cottontail.client.language.basics.expression.Literal
import org.vitrivr.cottontail.client.language.basics.predicate.Compare
import org.vitrivr.cottontail.client.language.dml.BatchInsert
import org.vitrivr.cottontail.client.language.dml.Insert
import org.vitrivr.cottontail.client.language.dml.Update
import org.vitrivr.cottontail.core.values.UuidValue
import org.vitrivr.engine.core.model.descriptor.scalar.StringDescriptor
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.plugin.cottontaildb.*
import org.vitrivr.engine.plugin.cottontaildb.descriptors.AbstractDescriptorWriter
import org.vitrivr.engine.plugin.cottontaildb.descriptors.vector.VectorDescriptorWriter

private val logger: KLogger = KotlinLogging.logger {}

/**
 * An [AbstractDescriptorWriter] for [StringDescriptor]s.
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
class StringDescriptorWriter(field: Schema.Field<*, StringDescriptor>, connection: CottontailConnection) : AbstractDescriptorWriter<StringDescriptor>(field, connection) {

    /**
     * Adds (writes) a single [StringDescriptor] using this [StringDescriptorWriter].
     *
     * @param item The [StringDescriptor] to write.
     * @return True on success, false otherwise.
     */
    override fun add(item: StringDescriptor): Boolean {
        val insert = Insert(this.entityName).values(
            DESCRIPTOR_ID_COLUMN_NAME to UuidValue(item.id),
            RETRIEVABLE_ID_COLUMN_NAME to UuidValue(item.retrievableId ?: throw IllegalArgumentException("A string descriptor must be associated with a retrievable ID.")),
            DESCRIPTOR_COLUMN_NAME to item.toValue()
        )
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
     * Adds (writes) a batch of [StringDescriptor] using this [StringDescriptorWriter].
     *
     * @param items A [Iterable] of [StringDescriptor]s to write.
     * @return True on success, false otherwise.
     */
    override fun addAll(items: Iterable<StringDescriptor>): Boolean {
        /* Prepare insert query. */
        var size = 0
        val insert = BatchInsert(this.entityName).columns(DESCRIPTOR_ID_COLUMN_NAME, RETRIEVABLE_ID_COLUMN_NAME, DESCRIPTOR_COLUMN_NAME)
        for (item in items) {
            size += 1
            insert.values(UuidValue(item.id.toString()), UuidValue(item.retrievableId ?: throw IllegalArgumentException("A string descriptor must be associated with a retrievable ID.")), item.toValue())
        }

        /* Insert values. */
        return try {
            this.connection.client.insert(insert).use {
                it.hasNext()
            }
        } catch (e: StatusRuntimeException) {
            logger.error(e) { "Failed to persist $size string descriptors due to exception." }
            false
        }
    }

    /**
     * Updates a specific [StringDescriptor] using this [VectorDescriptorWriter].
     *
     * @param item A [StringDescriptor]s to update.
     * @return True on success, false otherwise.
     */
    override fun update(item: StringDescriptor): Boolean {
        val update = Update(this.entityName).where(
            Compare(
                Column(this.entityName.column(DESCRIPTOR_ID_COLUMN_NAME)),
                Compare.Operator.EQUAL,
                Literal(UuidValue(item.id))
            )
        ).values(DESCRIPTOR_COLUMN_NAME to item.toValue())

        /* Updates values. */
        return try {
            this.connection.client.update(update)
            true
        } catch (e: StatusRuntimeException) {
            logger.error(e) { "Failed to update descriptor due to exception." }
            false
        }
    }
}