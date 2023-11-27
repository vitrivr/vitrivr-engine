package org.vitrivr.engine.base.database.cottontail.descriptors.scalar

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
import org.vitrivr.engine.base.database.cottontail.CottontailConnection.Companion.DESCRIPTOR_ID_COLUMN_NAME
import org.vitrivr.engine.base.database.cottontail.CottontailConnection.Companion.RETRIEVABLE_ID_COLUMN_NAME
import org.vitrivr.engine.base.database.cottontail.descriptors.AbstractDescriptorWriter
import org.vitrivr.engine.base.database.cottontail.descriptors.DESCRIPTOR_COLUMN_NAME
import org.vitrivr.engine.base.database.cottontail.descriptors.toValue
import org.vitrivr.engine.base.database.cottontail.descriptors.vector.VectorDescriptorWriter
import org.vitrivr.engine.core.model.descriptor.scalar.ScalarDescriptor
import org.vitrivr.engine.core.model.descriptor.vector.FloatVectorDescriptor
import org.vitrivr.engine.core.model.descriptor.vector.VectorDescriptor
import org.vitrivr.engine.core.model.metamodel.Schema

private val logger: KLogger = KotlinLogging.logger {}

/**
 * An [AbstractDescriptorWriter] for [FloatVectorDescriptor]s.
 *
 * @author Ralph Gasser
 * @version 1.0.0
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
            DESCRIPTOR_ID_COLUMN_NAME to StringValue(item.id.toString()),
            RETRIEVABLE_ID_COLUMN_NAME to StringValue(item.retrievableId.toString()),
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
     * Adds (writes) a batch of [ScalarDescriptor] using this [ScalarDescriptorWriter].
     *
     * @param items A [Iterable] of [ScalarDescriptor]s to write.
     * @return True on success, false otherwise.
     */
    override fun addAll(items: Iterable<ScalarDescriptor<*>>): Boolean {
        /* Prepare insert query. */
        var size = 0
        val insert = BatchInsert(this.entityName).columns(DESCRIPTOR_ID_COLUMN_NAME, RETRIEVABLE_ID_COLUMN_NAME, DESCRIPTOR_COLUMN_NAME)
        for (item in items) {
            size += 1
            insert.values(StringValue(item.id.toString()), StringValue(item.retrievableId.toString()), item.toValue())
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
     * Updates a specific [VectorDescriptor] using this [VectorDescriptorWriter].
     *
     * @param item A [VectorDescriptor]s to update.
     * @return True on success, false otherwise.
     */
    override fun update(item: ScalarDescriptor<*>): Boolean {
        val update = Update(this.entityName).where(
            Compare(
                Column(this.entityName.column(DESCRIPTOR_ID_COLUMN_NAME)),
                Compare.Operator.EQUAL,
                Literal(item.id.toString())
            )
        ).values(DESCRIPTOR_COLUMN_NAME to item.toValue())

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