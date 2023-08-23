package org.vitrivr.engine.core.database

import org.vitrivr.engine.core.database.descriptor.DescriptorInitializer
import org.vitrivr.engine.core.database.descriptor.DescriptorProvider
import org.vitrivr.engine.core.database.descriptor.DescriptorReader
import org.vitrivr.engine.core.database.descriptor.DescriptorWriter
import org.vitrivr.engine.core.model.database.descriptor.Descriptor
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.metamodel.Analyser

/**
 * An abstract implementation of the [Connection] interface.
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
@Suppress("UNCHECKED_CAST")
abstract class AbstractConnection(override val schemaName: String, private val provider: ConnectionProvider): Connection {

    /** An internal [Map] of registered [Reader], [Writer] and [Initializer] instances. */
    private val readers: Map<Schema.Field<*>,DescriptorReader<*>> = HashMap()

    /** An internal [Map] of registered [Reader], [Writer] and [Initializer] instances. */
    private val writers: Map<Schema.Field<*>,DescriptorWriter<*>> = HashMap()

    private val initializers: Map<Schema.Field<*>,DescriptorInitializer<*>> = HashMap()

    /**
     * Initializes the database layer with the [Schema] used by this [Connection].
     */
    override fun initialize() {
        this.initializers.values.forEach { it.initialize() }
    }

    /**
     * Truncates the database layer with the [Schema] used by this [Connection].
     */
    override fun truncate() {
        this.initializers.values.forEach { it.initialize() }
    }

    /**
     * Returns a cached version of the [DescriptorInitializer] for the provided [Analyser].
     *
     * [Analyser] must be part of the registered schema.
     *
     * @param field The [Analyser] to return the [DescriptorInitializer] for.
     * @return The [DescriptorInitializer]
     */
    override fun <T : Descriptor> getDescriptorInitializer(field: Schema.Field<T>): DescriptorInitializer<T> {
        var initializer = this.initializers[field]
        if (initializer == null) {
            val descriptorProvider = provider.obtain(field.analyser.descriptorClass) as? DescriptorProvider<Descriptor>
                ?: throw IllegalArgumentException("Unhandled describer $field for provided schema.")
            initializer = descriptorProvider.newInitializer(this, field as Schema.Field<Descriptor>)
            (this.initializers as HashMap)[field] = initializer
        }
        require(initializer.field == field) { "Misconfigured initializer; describers do not match. This is a programmer's error." }
        return initializer as DescriptorInitializer<T>
    }

    /**
     * Returns a cached version of the [DescriptorReader] for the provided [Analyser].
     *
     * [Analyser] must be part of the registered schema.
     *
     * @param field The [Analyser] to return the [DescriptorReader] for.
     * @return The [DescriptorReader]
     */
    override fun <T : Descriptor> getDescriptorReader(field: Schema.Field<T>): DescriptorReader<T> {
        var reader = this.readers[field]
        if (reader == null) {
            val descriptorProvider = provider.obtain(field.analyser.descriptorClass) as? DescriptorProvider<Descriptor>
                ?: throw IllegalArgumentException("Unhandled describer $field for provided schema.")
            reader = descriptorProvider.newReader(this, field as Schema.Field<Descriptor>)
            (this.readers as HashMap)[field] = reader
        }
        require(reader.field == field) { "Misconfigured reader; describers do not match. This is a programmer's error." }
        return reader as DescriptorReader<T>
    }

    /**
     * Returns a cached version of the [DescriptorWriter] for the provided [Analyser].
     *
     * [Analyser] must be part of the registered schema.
     *
     * @param field The [Analyser] to return the [DescriptorWriter] for.
     * @return The [DescriptorWriter]
     */
    override fun <T : Descriptor> getDescriptorWriter(field: Schema.Field<T>): DescriptorWriter<T> {
        var writer = this.writers[field]
        if (writer == null) {
            val descriptorProvider = provider.obtain(field.analyser.descriptorClass) as? DescriptorProvider<Descriptor>
                ?: throw IllegalArgumentException("Unhandled describer $field for provided schema.")
            writer = descriptorProvider.newWriter(this, field as Schema.Field<Descriptor>)
            (this.writers as HashMap)[field] = writer
        }
        require(writer.field == field) { "Misconfigured writer; describers do not match. This is a programmer's error." }
        return writer as DescriptorWriter<T>
    }
}