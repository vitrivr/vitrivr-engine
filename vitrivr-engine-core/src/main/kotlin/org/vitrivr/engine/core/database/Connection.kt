package org.vitrivr.engine.core.database

import org.vitrivr.engine.core.database.descriptor.DescriptorInitializer
import org.vitrivr.engine.core.database.descriptor.DescriptorReader
import org.vitrivr.engine.core.database.descriptor.DescriptorWriter
import org.vitrivr.engine.core.database.retrievable.RetrievableInitializer
import org.vitrivr.engine.core.database.retrievable.RetrievableReader
import org.vitrivr.engine.core.database.retrievable.RetrievableWriter
import org.vitrivr.engine.core.model.database.descriptor.Descriptor
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.metamodel.Field
import java.io.Closeable

/**
 * A database [Connection] that can be implemented.
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
sealed interface Connection: Closeable {
    /** The [Schema] used with this [Connection]. */
    val schema: Schema

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
    fun getRetrievableInitializer(): RetrievableInitializer

    /**
     * Returns a [RetrievableWriter].
     *
     * It remains up to the implementation, whether the [RetrievableWriter] returned by this method is
     * re-used or re-created every time the method is being called.
     */
    fun getRetrievableWriter(): RetrievableWriter

    /**
     * Returns a [RetrievableReader].
     *
     * It remains up to the implementation, whether the [RetrievableReader] returned by this method is
     * re-used or re-created every time the method is being called.
     */
    fun getRetrievableReader(): RetrievableReader

    /**
     * Returns a [DescriptorInitializer].
     *
     * @param field The [Field] to return the [DescriptorInitializer] for.
     *
     * It remains up to the implementation, whether the [DescriptorInitializer] returned by this method is
     * re-used or re-created every time the method is being called.
     */
    fun <T: Descriptor> getDescriptorInitializer(field: Field<T>): DescriptorInitializer<T>

    /**
     * Returns a [DescriptorWriter].
     *
     * @param field The [Field] to return the [DescriptorWriter] for.
     *
     * It remains up to the implementation, whether the [DescriptorWriter] returned by this method is
     * re-used or re-created every time the method is being called.
     */
    fun <T: Descriptor> getDescriptorWriter(field: Field<T>): DescriptorWriter<T>

    /**
     * Returns a [DescriptorReader].
     *
     * @param field The [Field] to return the [DescriptorReader] for.
     *
     * It remains up to the implementation, whether the [DescriptorReader] returned by this method is
     * re-used or re-created every time the method is being called.
     */
    fun <T: Descriptor> getDescriptorReader(field: Field<T>): DescriptorReader<T>
}