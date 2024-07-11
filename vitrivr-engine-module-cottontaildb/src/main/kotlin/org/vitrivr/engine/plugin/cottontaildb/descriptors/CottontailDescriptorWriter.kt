package org.vitrivr.engine.plugin.cottontaildb.descriptors

import io.grpc.StatusRuntimeException
import org.vitrivr.cottontail.client.language.basics.expression.Column
import org.vitrivr.cottontail.client.language.basics.expression.Literal
import org.vitrivr.cottontail.client.language.basics.predicate.Compare
import org.vitrivr.cottontail.client.language.dml.BatchInsert
import org.vitrivr.cottontail.client.language.dml.Delete
import org.vitrivr.cottontail.client.language.dml.Insert
import org.vitrivr.cottontail.client.language.dml.Update
import org.vitrivr.cottontail.core.database.Name
import org.vitrivr.cottontail.core.values.UuidValue
import org.vitrivr.engine.core.database.descriptor.DescriptorWriter
import org.vitrivr.engine.core.model.descriptor.Descriptor
import org.vitrivr.engine.core.model.descriptor.struct.StructDescriptor
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.plugin.cottontaildb.*

/**
 * An abstract implementation of a [DescriptorWriter] for Cottontail DB.
 *
 * @author Ralph Gasser
 * @version 1.1.0
 */
open class CottontailDescriptorWriter<D : Descriptor>(final override val field: Schema.Field<*, D>, override val connection: CottontailConnection) : DescriptorWriter<D> {
    /** The [Name.EntityName] used by this [Descriptor]. */
    protected val entityName: Name.EntityName = Name.EntityName.create(this.field.schema.name, "${DESCRIPTOR_ENTITY_PREFIX}_${this.field.fieldName.lowercase()}")

    /**
     * Adds (writes) a single [StructDescriptor] using this [CottontailDescriptorWriter].
     *
     * @param item The [StructDescriptor] to write.
     * @return True on success, false otherwise.
     */
    override fun add(item: D): Boolean {
        val insert = Insert(this.entityName).values(
            DESCRIPTOR_ID_COLUMN_NAME to UuidValue(item.id),
            RETRIEVABLE_ID_COLUMN_NAME to UuidValue(item.retrievableId ?: throw IllegalArgumentException("A struct descriptor must be associated with a retrievable ID.")),
        )

        /* Append fields. */
        for ((attribute, value) in item.values()) {
            insert.value(attribute, value?.toCottontailValue())
        }

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
     * Adds (writes) a batch of [StructDescriptor] using this [CottontailDescriptorWriter].
     *
     * @param items A [Iterable] of [StructDescriptor]s to write.
     * @return True on success, false otherwise.
     */
    override fun addAll(items: Iterable<D>): Boolean {
        /* Prepare insert query. */
        var index = 0
        val insert = BatchInsert(this.entityName)

        /* Insert values. */
        for (item in items) {
            val attributes = item.schema()
            if (index == 0) {
                val columns = Array(attributes.size + 2) {
                    when (it) {
                        0 -> DESCRIPTOR_ID_COLUMN_NAME
                        1 -> RETRIEVABLE_ID_COLUMN_NAME
                        else -> attributes[it - 2].name
                    }
                }
                insert.columns(*columns)
            }
            val inserts: Array<Any?> = Array(attributes.size + 2) {
                when (it) {
                    0 -> UuidValue(item.id)
                    1 -> item.retrievableId?.let { v -> UuidValue(v) }
                    else -> item.values()[attributes[it - 2].name]?.toCottontailValue()
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
            LOGGER.error(e) { "Failed to persist ${index + 1} scalar descriptors due to exception." }
            false
        }
    }

    /**
     * Updates a specific [Descriptor] of type [D] using this [CottontailDescriptorWriter].
     *
     * @param item A [Descriptor]s to update.
     * @return True on success, false otherwise.
     */
    override fun update(item: D): Boolean {
        val update = Update(this.entityName).where(
            Compare(
                Column(this.entityName.column(DESCRIPTOR_ID_COLUMN_NAME)),
                Compare.Operator.EQUAL,
                Literal(UuidValue(item.id))
            )
        )

        /* Append values. */
        for ((field, value) in item.values()) {
            update.any(field to value?.toCottontailValue())
        }

        /* Update values. */
        return try {
            this.connection.client.update(update)
            true
        } catch (e: StatusRuntimeException) {
            LOGGER.error(e) { "Failed to update descriptor due to exception." }
            false
        }
    }

    /**
     * Deletes (writes) a [Descriptor] of type [D] using this [CottontailDescriptorWriter].
     *
     * @param item A [Descriptor]s to delete.
     * @return True on success, false otherwise.
     */
    override fun delete(item: D): Boolean {
        val delete = Delete(this.entityName).where(
            Compare(
                Column(this.entityName.column(DESCRIPTOR_ID_COLUMN_NAME)),
                Compare.Operator.EQUAL,
                Literal(UuidValue(item.id))
            )
        )

        /* Delete values. */
        return try {
            this.connection.client.delete(delete).use {
                it.hasNext()
            }
        } catch (e: StatusRuntimeException) {
            LOGGER.error(e) { "Failed to delete scalar descriptor due to exception." }
            false
        }
    }

    /**
     * Deletes (writes) [Descriptor]s of type [D] using this [CottontailDescriptorWriter].
     *
     * @param items A [Iterable] of [Descriptor]s to delete.
     * @return True on success, false otherwise.
     */
    override fun deleteAll(items: Iterable<D>): Boolean {
        val ids = items.map { UuidValue(it.id) }
        val delete = Delete(this.entityName).where(
            Compare(
                Column(this.entityName.column(DESCRIPTOR_ID_COLUMN_NAME)),
                Compare.Operator.IN,
                org.vitrivr.cottontail.client.language.basics.expression.List(ids.toTypedArray())
            )
        )

        /* Delete values. */
        return try {
            this.connection.client.delete(delete).use {
                it.hasNext()
            }
        } catch (e: StatusRuntimeException) {
            LOGGER.error(e) { "Failed to delete descriptor due to exception." }
            false
        }
    }
}