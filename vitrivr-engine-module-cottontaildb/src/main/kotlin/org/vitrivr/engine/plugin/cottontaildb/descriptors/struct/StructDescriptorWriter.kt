package org.vitrivr.engine.plugin.cottontaildb.descriptors.struct

import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import io.grpc.StatusRuntimeException
import org.vitrivr.cottontail.client.language.basics.expression.Column
import org.vitrivr.cottontail.client.language.basics.expression.Literal
import org.vitrivr.cottontail.client.language.basics.predicate.Compare
import org.vitrivr.cottontail.client.language.dml.BatchInsert
import org.vitrivr.cottontail.client.language.dml.Insert
import org.vitrivr.cottontail.client.language.dml.Update
import org.vitrivr.cottontail.core.values.*
import org.vitrivr.engine.core.model.descriptor.struct.StructDescriptor
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.types.Value
import org.vitrivr.engine.plugin.cottontaildb.CottontailConnection
import org.vitrivr.engine.plugin.cottontaildb.DESCRIPTOR_ID_COLUMN_NAME
import org.vitrivr.engine.plugin.cottontaildb.RETRIEVABLE_ID_COLUMN_NAME
import org.vitrivr.engine.plugin.cottontaildb.descriptors.AbstractDescriptorWriter
import java.util.*

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
            DESCRIPTOR_ID_COLUMN_NAME to UuidValue(item.id),
            RETRIEVABLE_ID_COLUMN_NAME to UuidValue(item.retrievableId ?: throw IllegalArgumentException("A struct descriptor must be associated with a retrievable ID.")),
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
        var index = 0
        val insert = BatchInsert(this.entityName)

        /* Insert values. */
        for (item in items) {
            val values = item.values()
            if (index == 0) {
                val columns = Array(values.size + 2) {
                    when (it) {
                        0 -> DESCRIPTOR_ID_COLUMN_NAME
                        1 -> RETRIEVABLE_ID_COLUMN_NAME
                        else -> values[it - 2].first
                    }
                }
                insert.columns(*columns)
            }
            val inserts: Array<Any?> = Array(values.size + 2) {
                when (it) {
                    0 -> UuidValue(item.id)
                    1 -> item.retrievableId?.let { v -> UuidValue(v) }
                    else -> when (val v = values[it - 2].second) {
                        null -> null
                        is UUID -> UuidValue(v)
                        is String -> StringValue(v)
                        is Value.String -> StringValue(v.value)
                        is Boolean -> BooleanValue(v)
                        is Value.Boolean -> BooleanValue(v.value)
                        is Byte -> ByteValue(v)
                        is Value.Byte -> ByteValue(v.value)
                        is Short -> ShortValue(v)
                        is Value.Short -> ShortValue(v.value)
                        is Int -> IntValue(v)
                        is Value.Int -> IntValue(v.value)
                        is Long -> LongValue(v)
                        is Value.Long -> LongValue(v.value)
                        is Float -> FloatValue(v)
                        is Value.Float -> FloatValue(v.value)
                        is Double -> DoubleValue(v)
                        is Value.Double -> DoubleValue(v.value)
                        is Date -> DateValue(v)
                        is Value.DateTime -> DateValue(v.value)
                        else -> throw IllegalArgumentException("Unsupported type ${v::class.simpleName} for struct descriptor.")
                    }
                }
            }
            insert.any(*inserts)
            index += 1
        }

        /* Insert values. */
        return try {
            this.connection.client.insert(insert).use {
                it.hasNext()
            }
        } catch (e: StatusRuntimeException) {
            logger.error(e) { "Failed to persist ${index + 1} scalar descriptors due to exception." }
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
                Column(this.entityName.column(DESCRIPTOR_ID_COLUMN_NAME)),
                Compare.Operator.EQUAL,
                Literal(UuidValue(item.id))
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