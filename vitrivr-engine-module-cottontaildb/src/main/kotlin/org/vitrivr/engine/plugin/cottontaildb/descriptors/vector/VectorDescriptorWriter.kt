package org.vitrivr.engine.plugin.cottontaildb.descriptors.vector

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
import org.vitrivr.engine.core.model.descriptor.vector.VectorDescriptor
import org.vitrivr.engine.core.model.descriptor.vector.VectorDescriptor.Companion.VECTOR_ATTRIBUTE_NAME
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.plugin.cottontaildb.CottontailConnection
import org.vitrivr.engine.plugin.cottontaildb.DESCRIPTOR_ID_COLUMN_NAME
import org.vitrivr.engine.plugin.cottontaildb.RETRIEVABLE_ID_COLUMN_NAME
import org.vitrivr.engine.plugin.cottontaildb.descriptors.AbstractDescriptorWriter
import org.vitrivr.engine.plugin.cottontaildb.toCottontailValue

private val logger: KLogger = KotlinLogging.logger {}

/**
 * An [AbstractDescriptorWriter] for [VectorDescriptor]s.
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
class VectorDescriptorWriter(field: Schema.Field<*, VectorDescriptor<*>>, connection: CottontailConnection) : AbstractDescriptorWriter<VectorDescriptor<*>>(field, connection) {

    /**
     * Adds (writes) a single [VectorDescriptor] using this [VectorDescriptorWriter].
     *
     * @param item The [VectorDescriptor] to write.
     * @return True on success, false otherwise.
     */
    override fun add(item: VectorDescriptor<*>): Boolean {
        val insert = Insert(this.entityName).values(
            DESCRIPTOR_ID_COLUMN_NAME to UuidValue(item.id.toString()),
            RETRIEVABLE_ID_COLUMN_NAME to UuidValue(item.retrievableId ?: throw IllegalArgumentException("A vector descriptor must be associated with a retrievable ID.")),
            VECTOR_ATTRIBUTE_NAME to item.toCottontailValue()
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
     * Adds (writes) a batch of [VectorDescriptor] using this [VectorDescriptorWriter].
     *
     * @param items A [Iterable] of [VectorDescriptor]s to write.
     * @return True on success, false otherwise.
     */
    override fun addAll(items: Iterable<VectorDescriptor<*>>): Boolean {
        /* Prepare insert query. */
        var size = 0
        val insert = BatchInsert(this.entityName).columns(DESCRIPTOR_ID_COLUMN_NAME, RETRIEVABLE_ID_COLUMN_NAME, VECTOR_ATTRIBUTE_NAME)
        for (item in items) {
            size += 1
            insert.values(UuidValue(item.id), UuidValue(item.retrievableId ?: throw IllegalArgumentException("A vector descriptor must be associated with a retrievable ID.")), item.toCottontailValue())
        }

        /* Insert values. */
        return try {
            this.connection.client.insert(insert)
            true
        } catch (e: StatusRuntimeException) {
            logger.error(e) { "Failed to persist $size descriptors due to exception." }
            false
        }
    }

    /**
     * Updates a specific [VectorDescriptor] using this [VectorDescriptorWriter].
     *
     * @param item A [VectorDescriptor]s to update.
     * @return True on success, false otherwise.
     */
    override fun update(item: VectorDescriptor<*>): Boolean {
        val update = Update(this.entityName).where(
            Compare(
                Column(this.entityName.column(DESCRIPTOR_ID_COLUMN_NAME)),
                Compare.Operator.EQUAL,
                Literal(UuidValue(item.id))
            )
        ).values(VECTOR_ATTRIBUTE_NAME to item.toCottontailValue())

        /* Delete values. */
        return try {
            this.connection.client.update(update)
            true
        } catch (e: StatusRuntimeException) {
            logger.error(e) { "Failed to update descriptor due to exception." }
            false
        }
    }
}