package org.vitrivr.engine.base.database.cottontail.descriptors

import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import io.grpc.StatusRuntimeException
import org.vitrivr.cottontail.client.language.basics.expression.Column
import org.vitrivr.cottontail.client.language.basics.expression.Literal
import org.vitrivr.cottontail.client.language.basics.predicate.Compare
import org.vitrivr.cottontail.core.database.Name
import org.vitrivr.cottontail.core.tuple.Tuple
import org.vitrivr.cottontail.core.values.StringValue
import org.vitrivr.engine.base.database.cottontail.CottontailConnection
import org.vitrivr.engine.core.database.descriptor.DescriptorReader
import org.vitrivr.engine.core.model.descriptor.Descriptor
import org.vitrivr.engine.core.model.descriptor.DescriptorId
import org.vitrivr.engine.core.model.metamodel.Schema
import java.util.*

private val logger: KLogger = KotlinLogging.logger {}

/**
 * An abstract implementation of a [DescriptorReader] for Cottontail DB.
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
abstract class AbstractDescriptorReader<D : Descriptor>(final override val field: Schema.Field<*, D>, protected val connection: CottontailConnection) : DescriptorReader<D> {

    /** The [Name.EntityName] used by this [Descriptor]. */
    protected val entityName: Name.EntityName = Name.EntityName(this.field.schema.name, "${CottontailConnection.DESCRIPTOR_ENTITY_PREFIX}_${this.field.fieldName.lowercase()}")

    /**
     * Returns a single [Descriptor]s of type [D] that has the provided [UUID].
     *
     * @return [Sequence] of all [Descriptor]s.
     */
    override fun get(id: UUID): D? = getBy(id, CottontailConnection.DESCRIPTOR_ID_COLUMN_NAME)

    override fun getBy(id: UUID, columnName: String): D? {
        val query = org.vitrivr.cottontail.client.language.dql.Query(this.entityName)
            .where(Compare(Column(this.entityName.column(columnName)), Compare.Operator.EQUAL, Literal(id.toString())))
        return try {
            val result = this.connection.client.query(query)
            val ret = if (result.hasNext()) {
                this.tupleToDescriptor(result.next())
            } else {
                null
            }
            result.close()
            ret
        } catch (e: StatusRuntimeException) {
            logger.error(e) { "Failed to retrieve descriptor $id due to exception." }
            null
        }
    }

    /**
     * Checks whether a [Descriptor] of type [D] with the provided [UUID] exists.
     *
     * @param id The [DescriptorId], i.e., the [UUID] of the [Descriptor] to check for.
     * @return True if descriptor exsits, false otherwise
     */
    override fun exists(id: DescriptorId): Boolean {
        val query = org.vitrivr.cottontail.client.language.dql.Query(this.entityName).exists()
            .where(Compare(Column(this.entityName.column(CottontailConnection.DESCRIPTOR_ID_COLUMN_NAME)), Compare.Operator.EQUAL, Literal(id.toString())))
        return try {
            val result = this.connection.client.query(query)
            result.next().asBoolean(0) ?: false
        } catch (e: StatusRuntimeException) {
            logger.error(e) { "Failed to retrieve descriptor $id due to exception." }
            false
        }
    }

    /**
     * Returns all [Descriptor]s of type [D] as a [Sequence].
     *
     * @return [Sequence] of all [Descriptor]s.
     */
    override fun getAll(): Sequence<D> {
        val query = org.vitrivr.cottontail.client.language.dql.Query(this.entityName)
        return try {
            val result = this.connection.client.query(query)
            result.asSequence().map { this.tupleToDescriptor(it) }
        } catch (e: StatusRuntimeException) {
            /* TODO: Log. */
            emptySequence()
        }
    }

    /**
     * Returns all [Descriptor]s of type [D] that are contained in the provided [Iterable] of UUIDs as a [Sequence].
     *
     * @return [Sequence] of all [Descriptor]s.
     */
    override fun getAll(ids: Iterable<UUID>): Sequence<D> {
        val query = org.vitrivr.cottontail.client.language.dql.Query(this.entityName)
            .where(Compare(Column(this.entityName.column(CottontailConnection.DESCRIPTOR_ID_COLUMN_NAME)), Compare.Operator.IN, org.vitrivr.cottontail.client.language.basics.expression.List(ids.map { StringValue(it.toString()) }.toTypedArray())))
        return try {
            val result = this.connection.client.query(query)
            result.asSequence().map { this.tupleToDescriptor(it) }
        } catch (e: StatusRuntimeException) {
            /* TODO: Log. */
            emptySequence()
        }
    }

    /**
     * Returns the number of [Descriptor]s contained in the entity managed by this [AbstractDescriptorReader]
     *
     * @return [Sequence] of all [Descriptor]s.
     */
    override fun count(): Long {
        val query = org.vitrivr.cottontail.client.language.dql.Query(this.entityName).count()
        return try {
            val result = this.connection.client.query(query)
            val ret = if (result.hasNext()) {
                result.next().asLong(0) ?: 0L
            } else {
                0L
            }
            result.close()
            ret
        } catch (e: StatusRuntimeException) {
            /* TODO: Log. */
            0L
        }
    }

    /**
     * Converts a [Tuple] to a [Descriptor] of type [D].
     *
     * @param tuple The [Tuple] to convert.
     * @return [Descriptor] of type [D]
     */
    protected abstract fun tupleToDescriptor(tuple: Tuple): D
}