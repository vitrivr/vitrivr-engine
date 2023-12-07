package org.vitrivr.engine.base.database.cottontail.descriptors

import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import io.grpc.StatusRuntimeException
import org.vitrivr.cottontail.client.language.basics.expression.Column
import org.vitrivr.cottontail.client.language.basics.expression.Literal
import org.vitrivr.cottontail.client.language.basics.predicate.Compare
import org.vitrivr.cottontail.client.language.dml.Delete
import org.vitrivr.cottontail.core.database.Name
import org.vitrivr.cottontail.core.values.UuidValue
import org.vitrivr.engine.base.database.cottontail.CottontailConnection
import org.vitrivr.engine.core.database.descriptor.DescriptorWriter
import org.vitrivr.engine.core.model.descriptor.Descriptor
import org.vitrivr.engine.core.model.metamodel.Schema

private val logger: KLogger = KotlinLogging.logger {}

/**
 * An abstract implementation of a [DescriptorWriter] for Cottontail DB.

 * @author Ralph Gasser
 * @version 1.0.0
 */
abstract class AbstractDescriptorWriter<D : Descriptor>(final override val field: Schema.Field<*, D>, protected val connection: CottontailConnection) : DescriptorWriter<D> {
    /** The [Name.EntityName] used by this [Descriptor]. */
    protected val entityName: Name.EntityName = Name.EntityName(this.field.schema.name, "${CottontailConnection.DESCRIPTOR_ENTITY_PREFIX}_${this.field.fieldName.lowercase()}")

    /**
     * Deletes (writes) a [Descriptor] of type [D] using this [AbstractDescriptorWriter].
     *
     * @param item A [Descriptor]s to delete.
     * @return True on success, false otherwise.
     */
    override fun delete(item: D): Boolean {
        val delete = Delete(this.entityName).where(
            Compare(
                Column(this.entityName.column(CottontailConnection.DESCRIPTOR_ID_COLUMN_NAME)),
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
            logger.error(e) { "Failed to delete scalar descriptor due to exception." }
            false
        }
    }

    /**
     * Deletes (writes) [Descriptor]s of type [D] using this [AbstractDescriptorWriter].
     *
     * @param items A [Iterable] of [Descriptor]s to delete.
     * @return True on success, false otherwise.
     */
    override fun deleteAll(items: Iterable<D>): Boolean {
        val ids = items.map { UuidValue(it.id) }
        val delete = Delete(this.entityName).where(
            Compare(
                Column(this.entityName.column(CottontailConnection.DESCRIPTOR_ID_COLUMN_NAME)),
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
            logger.error(e) { "Failed to delete descriptor due to exception." }
            false
        }
    }
}