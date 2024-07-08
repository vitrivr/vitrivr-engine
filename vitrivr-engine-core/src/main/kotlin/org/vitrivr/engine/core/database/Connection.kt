package org.vitrivr.engine.core.database

import io.javalin.openapi.OpenApiIgnore
import org.vitrivr.engine.core.database.descriptor.DescriptorInitializer
import org.vitrivr.engine.core.database.descriptor.DescriptorReader
import org.vitrivr.engine.core.database.descriptor.DescriptorWriter
import org.vitrivr.engine.core.database.retrievable.RetrievableInitializer
import org.vitrivr.engine.core.database.retrievable.RetrievableReader
import org.vitrivr.engine.core.database.retrievable.RetrievableWriter
import org.vitrivr.engine.core.model.descriptor.Descriptor
import org.vitrivr.engine.core.model.metamodel.Schema
import java.io.Closeable

/**
 * A database [Connection] that can be implemented.
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
sealed interface Connection: Closeable {

    /** The [ConnectionProvider] used to create this [Connection]. */
    val provider: ConnectionProvider

    /** The name of the [Schema] managed by this [Connection]. */
    val schemaName: String

    /**
     * Executes the provided action within a transaction (if supported by the database).
     *
     * @param action The action to execute within the transaction.
     */
    fun <T> withTransaction(action: (Unit) -> T): T

    /**
     * Initializes the database layer with the [Schema] used by this [Connection].
     */
    fun initialize()

    /**
     * Truncates the database layer with the [Schema] used by this [Connection].
     */
    fun truncate()

    /**
     * Returns a [RetrievableInitializer].
     *
     * It remains up to the implementation, whether the [RetrievableInitializer] returned by this method is
     * re-used or re-created every time the method is being called.
     */
    @OpenApiIgnore
    fun getRetrievableInitializer(): RetrievableInitializer

    /**
     * Returns a [RetrievableWriter].
     *
     * It remains up to the implementation, whether the [RetrievableWriter] returned by this method is
     * re-used or re-created every time the method is being called.
     */
    @OpenApiIgnore
    fun getRetrievableWriter(): RetrievableWriter

    /**
     * Returns a [RetrievableReader].
     *
     * It remains up to the implementation, whether the [RetrievableReader] returned by this method is
     * re-used or re-created every time the method is being called.
     */
    @OpenApiIgnore
    fun getRetrievableReader(): RetrievableReader

    /**
     * Returns a [DescriptorInitializer].
     *
     * @param field The [Schema.Field] to return the [DescriptorInitializer] for.
     *
     * It remains up to the implementation, whether the [DescriptorInitializer] returned by this method is
     * re-used or re-created every time the method is being called.
     */
    @OpenApiIgnore
    fun <D: Descriptor> getDescriptorInitializer(field: Schema.Field<*,D>): DescriptorInitializer<D>

    /**
     * Returns a [DescriptorWriter].
     *
     * @param field The [Schema.Field] to return the [DescriptorWriter] for.
     *
     * It remains up to the implementation, whether the [DescriptorWriter] returned by this method is
     * re-used or re-created every time the method is being called.
     */
    @OpenApiIgnore
    fun <D: Descriptor> getDescriptorWriter(field: Schema.Field<*,D>): DescriptorWriter<D>

    /**
     * Returns a [DescriptorReader].
     *
     * @param field The [Schema.Field] to return the [DescriptorReader] for.
     *
     * It remains up to the implementation, whether the [DescriptorReader] returned by this method is
     * re-used or re-created every time the method is being called.
     */
    @OpenApiIgnore
    fun <D: Descriptor> getDescriptorReader(field: Schema.Field<*,D>): DescriptorReader<D>

    /**
     * Returns a human-readable descriptor of this [Connection].
     *
     * @return Human-readable description of this [Connection].
     */
    @OpenApiIgnore
    fun description(): String
}
