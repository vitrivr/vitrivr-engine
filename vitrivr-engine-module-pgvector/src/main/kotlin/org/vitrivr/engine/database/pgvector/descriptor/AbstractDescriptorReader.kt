package org.vitrivr.engine.database.pgvector.descriptor

import org.vitrivr.engine.core.database.descriptor.DescriptorReader
import org.vitrivr.engine.core.model.descriptor.Descriptor
import org.vitrivr.engine.core.model.descriptor.DescriptorId
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.query.Query
import org.vitrivr.engine.core.model.retrievable.RetrievableId
import org.vitrivr.engine.core.model.retrievable.Retrieved
import org.vitrivr.engine.database.pgvector.*
import java.sql.ResultSet
import java.util.*

/**
 * An abstract implementation of a [DescriptorReader] for Cottontail DB.
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
abstract class AbstractDescriptorReader<D : Descriptor>(final override val field: Schema.Field<*, D>, override val connection: PgVectorConnection) : DescriptorReader<D> {

    /** The name of the table backing this [AbstractDescriptorReader]. */
    protected val tableName: String = "${DESCRIPTOR_ENTITY_PREFIX}_${this.field.fieldName}"


    /** The [Descriptor] prototype handled by this [AbstractDescriptorReader]. */
    protected val prototype: D= this.field.analyser.prototype(this.field)

    /**
     * Returns a single [Descriptor]s of type [D] that has the provided [DescriptorId].
     *
     * @param descriptorId The [DescriptorId], i.e., the [UUID] of the [Descriptor] to return.
     * @return [Sequence] of all [Descriptor]s.
     */
    override fun get(descriptorId: DescriptorId): D? {
        try {
            this.connection.jdbc.prepareStatement("SELECT * FROM $tableName WHERE $DESCRIPTOR_ID_COLUMN_NAME = ?").use { stmt ->
                stmt.setObject(1, descriptorId)
                stmt.executeQuery().use { res ->
                    if (res.next()) {
                        return this.rowToDescriptor(res)
                    } else {
                        return null
                    }
                }
            }
        } catch (e: Exception) {
            LOGGER.error(e) { "Failed to check fetch descriptor $descriptorId from '$tableName' due to SQL error." }
            return null
        }
    }

    /**
     * Returns the [Descriptor]s of type [D] that belong to the provided [RetrievableId].
     *
     * @param retrievableId The [RetrievableId] to search for.
     * @return [Sequence] of [Descriptor]  of type [D]
     */
    override fun getForRetrievable(retrievableId: RetrievableId): Sequence<D> = sequence {
        try {
            this@AbstractDescriptorReader.connection.jdbc.prepareStatement("SELECT * FROM $tableName WHERE $RETRIEVABLE_ID_COLUMN_NAME = ?").use { stmt ->
                stmt.setObject(1, retrievableId)
                val result = stmt.executeQuery().use { result ->
                    while (result.next()) {
                        yield(this@AbstractDescriptorReader.rowToDescriptor(result))
                    }
                }
            }
        } catch (e: Exception) {
            LOGGER.error(e) { "Failed to fetch descriptor for retrievable $retrievableId from '$tableName' due to SQL error." }
        }
    }

    /**
     * Checks whether a [Descriptor] of type [D] with the provided [UUID] exists.
     *
     * @param descriptorId The [DescriptorId], i.e., the [UUID] of the [Descriptor] to check for.
     * @return True if descriptor exsits, false otherwise
     */
    override fun exists(descriptorId: DescriptorId): Boolean {
        try {
            this.connection.jdbc.prepareStatement("SELECT count(*) FROM $tableName WHERE $DESCRIPTOR_ID_COLUMN_NAME = ?").use { stmt ->
                stmt.setObject(1, descriptorId)
                stmt.executeQuery().use { res ->
                    res.next()
                    return res.getLong(1) > 0L
                }
            }
        } catch (e: Exception) {
            LOGGER.error(e) { "Failed to check for descriptor $descriptorId in '$tableName' due to SQL error." }
            return false
        }
    }

    /**
     * Returns all [Descriptor]s of type [D] as a [Sequence].
     *
     * @return [Sequence] of all [Descriptor]s.
     */
    override fun getAll(): Sequence<D> = sequence {
        try {
            this@AbstractDescriptorReader.connection.jdbc.prepareStatement("SELECT * FROM $tableName").use { stmt ->
                stmt.executeQuery().use { result ->
                    while (result.next()) {
                        yield(this@AbstractDescriptorReader.rowToDescriptor(result))
                    }
                }
            }
        } catch (e: Throwable) {
            LOGGER.error(e) { "Failed to fetch descriptors from '$tableName' due to SQL error." }
        }
    }

    /**
     * Returns a [Sequence] of all [Descriptor]s whose [DescriptorId] is contained in the provided [Iterable].
     *
     * @param descriptorIds A [Iterable] of [DescriptorId]s to return.
     * @return [Sequence] of [Descriptor] of type [D]
     */
    override fun getAll(descriptorIds: Iterable<DescriptorId>): Sequence<D> = sequence {
        try {
            this@AbstractDescriptorReader.connection.jdbc.prepareStatement("SELECT * FROM $tableName WHERE $DESCRIPTOR_ID_COLUMN_NAME = ANY (?)").use { stmt ->
                val values = descriptorIds.map { it }.toTypedArray()
                stmt.setArray(1, this@AbstractDescriptorReader.connection.jdbc.createArrayOf("uuid", values))
                stmt.executeQuery().use { result ->
                    while (result.next()) {
                        yield(this@AbstractDescriptorReader.rowToDescriptor(result))
                    }
                }
            }
        } catch (e: Exception) {
            LOGGER.error(e) { "Failed to fetch descriptors from '$tableName' due to SQL error." }
        }
    }

    /**
     * Returns a [Sequence] of all [Descriptor] whose [RetrievableId] is contained in the provided [Iterable].
     *
     * @param retrievableIds A [Iterable] of [RetrievableId]s to return [Descriptor]s for
     * @return [Sequence] of [Descriptor] of type [D]
     */
    override fun getAllForRetrievable(retrievableIds: Iterable<RetrievableId>): Sequence<D> = sequence {
        try {
            this@AbstractDescriptorReader.connection.jdbc.prepareStatement("SELECT * FROM $tableName WHERE $RETRIEVABLE_ID_COLUMN_NAME = ANY (?)").use { stmt ->
                val values = retrievableIds.map { it }.toTypedArray()
                stmt.setArray(1, this@AbstractDescriptorReader.connection.jdbc.createArrayOf("uuid", values))
                stmt.executeQuery().use { result ->
                    while (result.next()) {
                        yield(this@AbstractDescriptorReader.rowToDescriptor(result))
                    }
                }
            }
        } catch (e: Exception) {
            LOGGER.error(e) { "Failed to fetch descriptors from '$tableName' due to SQL error." }
         }
    }

    /**
     * Returns the number of [Descriptor]s contained in the entity managed by this [AbstractDescriptorReader]
     *
     * @return [Sequence] of all [Descriptor]s.
     */
    override fun count(): Long {
        try {
            this.connection.jdbc.prepareStatement("SELECT COUNT(*) FROM $tableName;").use { stmt ->
                stmt.executeQuery().use { result ->
                    result.next()
                    return result.getLong(1)
                }
            }
        } catch (e: Exception) {
            LOGGER.error(e) { "Failed to count retrievable due to SQL error." }
            return 0L
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
        val retrievables = this.connection.getRetrievableReader().getAll(descriptors.mapNotNull { it.retrievableId }.toSet()).map { it.id to it }.toMap()
        return descriptors.asSequence().mapNotNull { descriptor ->
            val retrievable = retrievables[descriptor.retrievableId]
            if (retrievable != null) {
                retrievable.addDescriptor(descriptor)
                retrievable as Retrieved
            } else {
                null
            }
        }
    }

    /**
     * Converts a [ResultSet] to a [Descriptor] of type [D].
     *
     * @param result The [ResultSet] to convert.
     * @return [Descriptor] of type [D]
     */
    abstract fun rowToDescriptor(result: ResultSet): D
}