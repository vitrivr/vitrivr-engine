package org.vitrivr.engine.plugin.cottontaildb.descriptors.scalar

import io.grpc.StatusRuntimeException
import org.vitrivr.cottontail.client.language.basics.expression.Column
import org.vitrivr.cottontail.client.language.basics.expression.Literal
import org.vitrivr.cottontail.client.language.basics.predicate.Compare
import org.vitrivr.cottontail.client.language.dml.BatchInsert
import org.vitrivr.cottontail.client.language.dml.Insert
import org.vitrivr.cottontail.client.language.dml.Update
import org.vitrivr.cottontail.core.values.UuidValue
import org.vitrivr.engine.core.model.descriptor.scalar.ScalarDescriptor
import org.vitrivr.engine.core.model.descriptor.scalar.ScalarDescriptor.Companion.VALUE_ATTRIBUTE_NAME
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.plugin.cottontaildb.*
import org.vitrivr.engine.plugin.cottontaildb.descriptors.AbstractDescriptorWriter

/**
 * An [AbstractDescriptorWriter] for [ScalarDescriptor]s.
 *
 * @author Ralph Gasser
 * @version 1.0.1
 */
class ScalarDescriptorWriter(field: Schema.Field<*, ScalarDescriptor<*>>, connection: CottontailConnection) : AbstractDescriptorWriter<ScalarDescriptor<*>>(field, connection) {

    /**
     * Adds (writes) a single [ScalarDescriptor] using this [ScalarDescriptorWriter].
     *
     * @param item The [ScalarDescriptor] to write.
     * @return True on success, false otherwise.
     */
    override fun add(item: ScalarDescriptor<*>): Boolean {
        val insert = Insert(this.entityName).values(
            DESCRIPTOR_ID_COLUMN_NAME to UuidValue(item.id),
            RETRIEVABLE_ID_COLUMN_NAME to UuidValue(item.retrievableId ?: throw IllegalArgumentException("A scalar descriptor must be associated with a retrievable ID.")),
            VALUE_ATTRIBUTE_NAME to item.toCottontailValue()
        )
        return try {
            this.connection.client.insert(insert).use {
                it.hasNext()
            }
        } catch (e: StatusRuntimeException) {
            LOGGER.error(e) { "Failed to persist descriptor ${item.id} due to exception." }
            false
        }
    }

    /**
     * Adds (writes) a batch of [ScalarDescriptor] using this [ScalarDescriptorWriter].
     *
     * @param items A [Iterable] of [ScalarDescriptor]s to write.
     * @return True on success, false otherwise.
     */
    override fun addAll(items: Iterable<ScalarDescriptor<*>>): Boolean {
        /* Prepare insert query. */
        var size = 0
        val insert = BatchInsert(this.entityName).columns(DESCRIPTOR_ID_COLUMN_NAME, RETRIEVABLE_ID_COLUMN_NAME, VALUE_ATTRIBUTE_NAME)
        for (item in items) {
            size += 1
            insert.values(UuidValue(item.id.toString()), UuidValue(item.retrievableId ?: throw IllegalArgumentException("A scalar descriptor must be associated with a retrievable ID.")), item.toCottontailValue())
        }

        /* Insert values. */
        return try {
            this.connection.client.insert(insert).use {
                it.hasNext()
            }
        } catch (e: StatusRuntimeException) {
            LOGGER.error(e) { "Failed to persist $size scalar descriptors due to exception." }
            false
        }
    }

    /**
     * Updates a specific [ScalarDescriptor] using this [ScalarDescriptorWriter].
     *
     * @param item A [ScalarDescriptor]s to update.
     * @return True on success, false otherwise.
     */
    override fun update(item: ScalarDescriptor<*>): Boolean {
        val update = Update(this.entityName).where(
            Compare(
                Column(this.entityName.column(DESCRIPTOR_ID_COLUMN_NAME)),
                Compare.Operator.EQUAL,
                Literal(UuidValue(item.id))
            )
        ).values(VALUE_ATTRIBUTE_NAME to item.toCottontailValue())

        /* Delete values. */
        return try {
            this.connection.client.update(update)
            true
        } catch (e: StatusRuntimeException) {
            LOGGER.error(e) { "Failed to update descriptor due to exception." }
            false
        }
    }
}