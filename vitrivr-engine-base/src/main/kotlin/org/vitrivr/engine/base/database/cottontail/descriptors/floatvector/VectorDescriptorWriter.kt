package org.vitrivr.engine.base.database.cottontail.descriptors.floatvector

import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import io.grpc.StatusException
import org.vitrivr.cottontail.client.language.basics.expression.Column
import org.vitrivr.cottontail.client.language.basics.expression.Literal
import org.vitrivr.cottontail.client.language.basics.predicate.Compare
import org.vitrivr.cottontail.client.language.dml.BatchInsert
import org.vitrivr.cottontail.client.language.dml.Delete
import org.vitrivr.cottontail.client.language.dml.Insert
import org.vitrivr.cottontail.core.values.StringValue
import org.vitrivr.engine.base.database.cottontail.CottontailConnection
import org.vitrivr.engine.base.database.cottontail.CottontailConnection.Companion.DESCRIPTOR_ID_COLUMN_NAME
import org.vitrivr.engine.base.database.cottontail.CottontailConnection.Companion.RETRIEVABLE_ID_COLUMN_NAME
import org.vitrivr.engine.base.database.cottontail.descriptors.DESCRIPTOR_COLUMN_NAME
import org.vitrivr.engine.base.database.cottontail.descriptors.toValue
import org.vitrivr.engine.base.database.cottontail.writer.AbstractDescriptorWriter
import org.vitrivr.engine.core.model.database.descriptor.vector.VectorDescriptor
import org.vitrivr.engine.core.model.metamodel.Schema

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
            DESCRIPTOR_ID_COLUMN_NAME to StringValue(item.id.toString()),
            RETRIEVABLE_ID_COLUMN_NAME to StringValue(item.retrievableId.toString()),
            DESCRIPTOR_COLUMN_NAME to item.toValue()
        )
        return try {
            this.connection.client.insert(insert)
            true
        } catch (e: StatusException) {
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
        val insert = BatchInsert(this.entityName).columns(DESCRIPTOR_ID_COLUMN_NAME, RETRIEVABLE_ID_COLUMN_NAME, DESCRIPTOR_COLUMN_NAME)
        for (item in items) {
            size += 1
            insert.values(StringValue(item.id.toString()), StringValue(item.retrievableId.toString()), item.toValue())
        }

        /* Insert values. */
        return try {
            this.connection.client.insert(insert)
            true
        } catch (e: StatusException) {
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
        TODO("Not yet implemented")
    }

    /**
     * Deletes (writes) a [VectorDescriptor] using this [VectorDescriptorWriter].
     *
     * @param item A [VectorDescriptor]s to delete.
     * @return True on success, false otherwise.
     */
    override fun delete(item: VectorDescriptor<*>): Boolean {
        val delete = Delete(this.entityName).where(
            Compare(
                Column(this.entityName.column(DESCRIPTOR_ID_COLUMN_NAME)),
                Compare.Operator.EQUAL,
                Literal(item.id.toString())
            )
        )

        /* Delete values. */
        return try {
            this.connection.client.delete(delete)
            true
        } catch (e: StatusException) {
            logger.error(e) { "Failed to delete descriptor due to exception." }
            false
        }
    }
}