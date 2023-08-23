package org.vitrivr.engine.base.database.cottontail.writer

import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import io.grpc.StatusException
import org.vitrivr.cottontail.client.language.dml.BatchInsert
import org.vitrivr.cottontail.client.language.dml.Insert
import org.vitrivr.engine.base.database.cottontail.CottontailConnection
import org.vitrivr.engine.core.model.database.descriptor.Descriptor
import org.vitrivr.engine.core.model.database.descriptor.vector.FloatVectorDescriptor
import org.vitrivr.engine.core.model.metamodel.Schema

private val logger: KLogger = KotlinLogging.logger {}

/**
 * An [AbstractDescriptorWriter] for [FloatVectorDescriptor]s.
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
class FloatVectorDescriptorWriter(field: Schema.Field<FloatVectorDescriptor>, connection: CottontailConnection): AbstractDescriptorWriter<FloatVectorDescriptor>(field, connection) {
    override fun add(item: FloatVectorDescriptor): Boolean {
        val insert = Insert(this.entityName).any("id" to item.id, "retrievableId" to item.retrievableId, "vector" to item.vector)
        return try {
            this.connection.client.insert(insert)
            true
        } catch (e: StatusException) {
            logger.error(e) { "Failed to persist descriptor ${item.id} due to exception." }
            false
        }
    }

    override fun addAll(items: Iterable<FloatVectorDescriptor>): Boolean {
        /* Prepare insert query. */
        var size = 0
        val insert = BatchInsert(this.entityName).columns("id", "retrievableId", "vector")
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