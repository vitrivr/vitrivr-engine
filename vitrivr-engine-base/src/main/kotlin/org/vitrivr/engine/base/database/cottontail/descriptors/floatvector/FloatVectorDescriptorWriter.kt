package org.vitrivr.engine.base.database.cottontail.descriptors.floatvector

import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import io.grpc.StatusException
import org.vitrivr.cottontail.client.language.dml.BatchInsert
import org.vitrivr.cottontail.client.language.dml.Insert
import org.vitrivr.engine.base.database.cottontail.CottontailConnection
import org.vitrivr.engine.base.database.cottontail.CottontailConnection.Companion.DESCRIPTOR_ID_COLUMN_NAME
import org.vitrivr.engine.base.database.cottontail.CottontailConnection.Companion.RETRIEVABLE_ID_COLUMN_NAME
import org.vitrivr.engine.base.database.cottontail.writer.AbstractDescriptorWriter
import org.vitrivr.engine.core.model.database.descriptor.vector.FloatVectorDescriptor
import org.vitrivr.engine.core.model.metamodel.Schema

private val logger: KLogger = KotlinLogging.logger {}



/**
 * An [AbstractDescriptorWriter] for [FloatVectorDescriptor]s.
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
class FloatVectorDescriptorWriter(field: Schema.Field<*, FloatVectorDescriptor>, connection: CottontailConnection): AbstractDescriptorWriter<FloatVectorDescriptor>(field, connection) {

    /**
     * Adds (writes) a single [FloatVectorDescriptor] using this [FloatVectorDescriptorWriter].
     *
     * @param item The [FloatVectorDescriptor] to write.
     * @return True on success, false otherwise.
     */
    override fun add(item: FloatVectorDescriptor): Boolean {
        val insert = Insert(this.entityName).any(DESCRIPTOR_ID_COLUMN_NAME to item.id, RETRIEVABLE_ID_COLUMN_NAME to item.retrievableId, FEATURE_COLUMN_NAME to item.vector)
        return try {
            this.connection.client.insert(insert)
            true
        } catch (e: StatusException) {
            logger.error(e) { "Failed to persist descriptor ${item.id} due to exception." }
            false
        }
    }

    /**
     * Adds (writes) a batch of [FloatVectorDescriptor] using this [FloatVectorDescriptorWriter].
     *
     * @param items A [Iterable] of [FloatVectorDescriptor]s to write.
     * @return True on success, false otherwise.
     */
    override fun addAll(items: Iterable<FloatVectorDescriptor>): Boolean {
        /* Prepare insert query. */
        var size = 0
        val insert = BatchInsert(this.entityName).columns(DESCRIPTOR_ID_COLUMN_NAME, RETRIEVABLE_ID_COLUMN_NAME, FEATURE_COLUMN_NAME)
        for (item in items) {
            size += 1
            insert.any(item.id, item.retrievableId, item.vector)
        }

        /* Insert values. */
        return try {
            this.connection.client.insert(insert)
            true
        } catch (e: StatusException) {
            logger.error(e) { "Failed to persist $size descroptors due to exception." }
            false
        }
    }

    override fun update(item: FloatVectorDescriptor): Boolean {
        TODO("Not yet implemented")
    }

    override fun delete(item: FloatVectorDescriptor): Boolean {
        TODO("Not yet implemented")
    }
}