package org.vitrivr.engine.base.database.cottontail.descriptors.label

import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import io.grpc.StatusException
import org.vitrivr.cottontail.client.language.basics.expression.Column
import org.vitrivr.cottontail.client.language.basics.expression.Literal
import org.vitrivr.cottontail.client.language.basics.predicate.Compare
import org.vitrivr.cottontail.client.language.dml.BatchInsert
import org.vitrivr.cottontail.client.language.dml.Insert
import org.vitrivr.cottontail.client.language.dml.Update
import org.vitrivr.cottontail.core.values.FloatValue
import org.vitrivr.cottontail.core.values.StringValue
import org.vitrivr.engine.base.database.cottontail.CottontailConnection
import org.vitrivr.engine.base.database.cottontail.descriptors.AbstractDescriptorWriter
import org.vitrivr.engine.base.database.cottontail.descriptors.DESCRIPTOR_COLUMN_NAME
import org.vitrivr.engine.core.model.descriptor.struct.LabelDescriptor
import org.vitrivr.engine.core.model.metamodel.Schema

private val logger: KLogger = KotlinLogging.logger {}

/**
 * An [AbstractDescriptorWriter] for [LabelDescriptor]s.
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
class LabelDescriptorWriter(field: Schema.Field<*, LabelDescriptor>, connection: CottontailConnection) : AbstractDescriptorWriter<LabelDescriptor>(field, connection) {

    /**
     * Adds (writes) a single [LabelDescriptor] using this [LabelDescriptorWriter].
     *
     * @param item The [LabelDescriptor] to write.
     * @return True on success, false otherwise.
     */
    override fun add(item: LabelDescriptor): Boolean {
        val insert = Insert(this.entityName).values(
            CottontailConnection.DESCRIPTOR_ID_COLUMN_NAME to StringValue(item.id.toString()),
            CottontailConnection.RETRIEVABLE_ID_COLUMN_NAME to StringValue(item.retrievableId.toString()),
            DESCRIPTOR_COLUMN_NAME to StringValue(item.label),
            CONFIDENCE_COLUMN_NAME to FloatValue(item.confidence)
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
     * Adds (writes) a batch of [LabelDescriptor] using this [LabelDescriptorWriter].
     *
     * @param items A [Iterable] of [LabelDescriptor]s to write.
     * @return True on success, false otherwise.
     */
    override fun addAll(items: Iterable<LabelDescriptor>): Boolean {
        /* Prepare insert query. */
        var size = 0
        val insert = BatchInsert(this.entityName).columns(CottontailConnection.DESCRIPTOR_ID_COLUMN_NAME, CottontailConnection.RETRIEVABLE_ID_COLUMN_NAME, DESCRIPTOR_COLUMN_NAME, CONFIDENCE_COLUMN_NAME)
        for (item in items) {
            size += 1
            insert.values(StringValue(item.id.toString()), StringValue(item.retrievableId.toString()), StringValue(item.label), FloatValue(item.confidence))
        }

        /* Insert values. */
        return try {
            this.connection.client.insert(insert)
            true
        } catch (e: StatusException) {
            logger.error(e) { "Failed to persist $size scalar descriptors due to exception." }
            false
        }
    }

    /**
     * Updates a specific [LabelDescriptor] using this [LabelDescriptorWriter].
     *
     * @param item A [LabelDescriptor]s to update.
     * @return True on success, false otherwise.
     */
    override fun update(item: LabelDescriptor): Boolean {
        val update = Update(this.entityName).where(
            Compare(
                Column(this.entityName.column(CottontailConnection.DESCRIPTOR_ID_COLUMN_NAME)),
                Compare.Operator.EQUAL,
                Literal(item.id.toString())
            )
        ).values(
            DESCRIPTOR_COLUMN_NAME to StringValue(item.label),
            CONFIDENCE_COLUMN_NAME to FloatValue(item.confidence),
        )

        /* Delete values. */
        return try {
            this.connection.client.update(update)
            true
        } catch (e: StatusException) {
            logger.error(e) { "Failed to update descriptor due to exception." }
            false
        }
    }
}