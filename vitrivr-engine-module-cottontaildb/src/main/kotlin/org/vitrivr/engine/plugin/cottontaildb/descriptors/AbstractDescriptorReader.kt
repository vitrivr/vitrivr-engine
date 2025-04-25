package org.vitrivr.engine.plugin.cottontaildb.descriptors

import io.grpc.StatusRuntimeException
import org.vitrivr.cottontail.client.language.basics.expression.Column
import org.vitrivr.cottontail.client.language.basics.expression.Literal
import org.vitrivr.cottontail.client.language.basics.expression.ValueList
import org.vitrivr.cottontail.client.language.basics.predicate.Compare
import org.vitrivr.cottontail.core.database.Name
import org.vitrivr.cottontail.core.tuple.Tuple
import org.vitrivr.cottontail.core.values.UuidValue
import org.vitrivr.engine.core.database.descriptor.DescriptorReader
import org.vitrivr.engine.core.model.descriptor.Descriptor
import org.vitrivr.engine.core.model.descriptor.DescriptorId
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.query.Query
import org.vitrivr.engine.core.model.retrievable.RetrievableId
import org.vitrivr.engine.core.model.retrievable.Retrieved
import org.vitrivr.engine.plugin.cottontaildb.*
import java.util.*

/**
 * An abstract implementation of a [DescriptorReader] for Cottontail DB.
 *
 * @author Ralph Gasser
 * @version 1.1.0
 */
abstract class AbstractDescriptorReader<D : Descriptor<*>>(final override val field: Schema.Field<*, D>, override val connection: CottontailConnection) : DescriptorReader<D> {

    /** The [Name.EntityName] used by this [Descriptor]. */
    protected val entityName: Name.EntityName = Name.EntityName.create(this.field.schema.name, "${DESCRIPTOR_ENTITY_PREFIX}_${this.field.fieldName.lowercase()}")

    /**
     * Returns a single [Descriptor]s of type [D] that has the provided [DescriptorId].
     *
     * @param descriptorId The [DescriptorId], i.e., the [UUID] of the [Descriptor] to return.
     * @return [Sequence] of all [Descriptor]s.
     */
    override fun get(descriptorId: DescriptorId): D? {
        val query = org.vitrivr.cottontail.client.language.dql.Query(this.entityName)
            .where(Compare(Column(this.entityName.column(DESCRIPTOR_ID_COLUMN_NAME)), Compare.Operator.EQUAL, Literal(UuidValue(descriptorId))))
        return try {
            this.connection.client.query(query).use { result ->
                val ret = if (result.hasNext()) {
                    this.tupleToDescriptor(result.next())
                } else {
                    null
                }
                ret
            }
        } catch (e: StatusRuntimeException) {
            LOGGER.error(e) { "Failed to retrieve descriptor $descriptorId due to exception." }
            null
        }
    }

    /**
     * Returns the [Descriptor]s of type [D] that belong to the provided [RetrievableId].
     *
     * @param retrievableId The [RetrievableId] to search for.
     * @return [Sequence] of [Descriptor]  of type [D]
     */
    override fun getForRetrievable(retrievableId: RetrievableId): Sequence<D> {
        val query = org.vitrivr.cottontail.client.language.dql.Query(this.entityName).where(Compare(Column(this.entityName.column(RETRIEVABLE_ID_COLUMN_NAME)), Compare.Operator.EQUAL, Literal(UuidValue(retrievableId))))
        return try {
            val result = this.connection.client.query(query)
            result.asSequence().map { this.tupleToDescriptor(it) }
        } catch (e: StatusRuntimeException) {
            LOGGER.error(e) { "Failed to retrieve descriptors for retrievable $retrievableId due to exception." }
            emptySequence()
        }
    }

    /**
     * Checks whether a [Descriptor] of type [D] with the provided [UUID] exists.
     *
     * @param descriptorId The [DescriptorId], i.e., the [UUID] of the [Descriptor] to check for.
     * @return True if descriptor exsits, false otherwise
     */
    override fun exists(descriptorId: DescriptorId): Boolean {
        val query = org.vitrivr.cottontail.client.language.dql.Query(this.entityName).exists()
            .where(Compare(Column(this.entityName.column(DESCRIPTOR_ID_COLUMN_NAME)), Compare.Operator.EQUAL, Literal(UuidValue(descriptorId))))
        return try {
            val result = this.connection.client.query(query)
            result.next().asBoolean(0) ?: false
        } catch (e: StatusRuntimeException) {
            LOGGER.error(e) { "Failed to retrieve descriptor $descriptorId due to exception." }
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
            LOGGER.error(e) { "Failed to retrieve descriptors due to exception." }
            emptySequence()
        }
    }

    /**
     * Returns a [Sequence] of all [Descriptor]s whose [DescriptorId] is contained in the provided [Iterable].
     *
     * @param descriptorIds A [Iterable] of [DescriptorId]s to return.
     * @return [Sequence] of [Descriptor] of type [D]
     */
    override fun getAll(descriptorIds: Iterable<UUID>): Sequence<D> {
        val query = org.vitrivr.cottontail.client.language.dql.Query(this.entityName)
            .where(Compare(Column(this.entityName.column(DESCRIPTOR_ID_COLUMN_NAME)), Compare.Operator.IN, ValueList(descriptorIds.map { UuidValue(it) }.toTypedArray())))
        return try {
            val result = this.connection.client.query(query)
            result.asSequence().map { this.tupleToDescriptor(it) }
        } catch (e: StatusRuntimeException) {
            LOGGER.error(e) { "Failed to retrieve descriptors for provided descriptor IDs due to exception." }
            emptySequence()
        }
    }

    /**
     * Returns a [Sequence] of all [Descriptor] whose [RetrievableId] is contained in the provided [Iterable].
     *
     * @param retrievableIds A [Iterable] of [RetrievableId]s to return [Descriptor]s for
     * @return [Sequence] of [Descriptor] of type [D]
     */
    override fun getAllForRetrievable(retrievableIds: Iterable<RetrievableId>): Sequence<D> {
        val query = org.vitrivr.cottontail.client.language.dql.Query(this.entityName)
            .where(Compare(Column(this.entityName.column(RETRIEVABLE_ID_COLUMN_NAME)), Compare.Operator.IN, ValueList(retrievableIds.map { UuidValue(it) }.toTypedArray())))
        return try {
            val result = this.connection.client.query(query)
            result.asSequence().map { this.tupleToDescriptor(it) }
        } catch (e: StatusRuntimeException) {
            LOGGER.error(e) { "Failed to retrieve descriptors for provided retrievable IDs due to exception." }
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
            LOGGER.error(e) { "Failed to count descriptors due to exception." }
            0L
        }
    }

    /**
     * Returns a [Sequence] of all [Retrieved]s that match the given [Query].
     *
     * Implicitly, this methods executes a [query] and then JOINS the result with the [Retrieved]s.
     *
     * @param query The [Query] that should be executed.
     * @return [Sequence] of [Retrieved].
     */
    override fun queryAndJoin(query: Query): Sequence<Retrieved> {
        val descriptors = query(query).toList()
        if (descriptors.isEmpty()) return emptySequence()

        /* Fetch retrievable ids. */
        val retrievables = this.fetchRetrievable(descriptors.mapNotNull { it.retrievableId }.toSet())
        return descriptors.asSequence().mapNotNull { descriptor ->
            val retrievable = retrievables[descriptor.retrievableId]
            retrievable?.copy(descriptors = retrievable.descriptors + descriptor)
        }
    }

    /**
     * Fetches the [Retrieved] specified by the provided [RetrievableId]s.
     *
     * @param ids The [RetrievableId] to fetch
     * @return A [Map] of [RetrievableId] to [Retrieved]
     */
    protected fun fetchRetrievable(ids: Iterable<RetrievableId>): Map<RetrievableId, Retrieved> {
        /* Prepare Cottontail DB query. */
        val entityName = this.entityName.schema().entity(RETRIEVABLE_ENTITY_NAME)
        val query = org.vitrivr.cottontail.client.language.dql.Query(entityName).select("*").where(
            Compare(
                Column(entityName.column(RETRIEVABLE_ID_COLUMN_NAME)),
                Compare.Operator.IN,
                ValueList(ids.map { UuidValue(it) }.toTypedArray())
            )
        )

        return this.connection.client.query(query).asSequence().associate { tuple ->
            val retrievableId = tuple.asUuidValue(RETRIEVABLE_ID_COLUMN_NAME)?.value ?: throw IllegalArgumentException("The provided tuple is missing the required field '$RETRIEVABLE_ID_COLUMN_NAME'.")
            val type = tuple.asString(RETRIEVABLE_TYPE_COLUMN_NAME) ?: throw IllegalArgumentException("The provided tuple is missing the required field '$RETRIEVABLE_TYPE_COLUMN_NAME'.")

            /* Prepare the retrieved. */
            val retrieved = Retrieved(retrievableId, type, transient = false)
            retrievableId to retrieved
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