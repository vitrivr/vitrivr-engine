package org.vitrivr.engine.core.database

import org.vitrivr.engine.core.database.descriptor.DescriptorInitializer
import org.vitrivr.engine.core.database.descriptor.DescriptorReader
import org.vitrivr.engine.core.database.descriptor.DescriptorWriter
import org.vitrivr.engine.core.model.descriptor.Descriptor
import org.vitrivr.engine.core.model.metamodel.Analyser
import org.vitrivr.engine.core.model.metamodel.Schema

/**
 * An abstract implementation of the [Connection] interface.
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
@Suppress("UNCHECKED_CAST")
abstract class AbstractConnection(override val schemaName: String, override val provider: ConnectionProvider) : Connection {

    /** An internal [Map] of registered [Reader], [Writer] and [Initializer] instances. */
    private val readers: Map<Schema.Field<*, Descriptor<*>>, DescriptorReader<*>> = HashMap()

    /** An internal [Map] of registered [Reader], [Writer] and [Initializer] instances. */
    private val writers: Map<Schema.Field<*, Descriptor<*>>, DescriptorWriter<*>> = HashMap()

    /** */
    private val initializers: Map<Schema.Field<*, Descriptor<*>>, DescriptorInitializer<*>> = HashMap()

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
    override fun <D : Descriptor<*>> getDescriptorInitializer(field: Schema.Field<*, D>): DescriptorInitializer<D> {
        var initializer = this.initializers[field as Schema.Field<*, Descriptor<*>>]
        if (initializer == null) {
            val descriptorProvider = this.provider.obtain(field.analyser.descriptorClass)
                ?: throw IllegalArgumentException("Unhandled describer $field for provided schema.")
            initializer = descriptorProvider.newInitializer(this, field as Schema.Field<*, Descriptor<*>>)
            (this.initializers as HashMap)[field] = initializer
        }
        require(initializer.field == field) { "Misconfigured initializer; describers do not match. This is a programmer's error." }
        return initializer as DescriptorInitializer<D>
    }

    /**
     * Returns a cached version of the [DescriptorReader] for the provided [Analyser].
     *
     * [Analyser] must be part of the registered schema.
     *
     * @param field The [Analyser] to return the [DescriptorReader] for.
     * @return The [DescriptorReader]
     */
    override fun <D : Descriptor<*>> getDescriptorReader(field: Schema.Field<*, D>): DescriptorReader<D> {
        var reader = this.readers[field as Schema.Field<*, Descriptor<*>>]
        if (reader == null) {
            val descriptorProvider = this.provider.obtain(field.analyser.descriptorClass)
                ?: throw IllegalArgumentException("Unhandled describer $field for provided schema.")
            reader = descriptorProvider.newReader(this, field as Schema.Field<*, Descriptor<*>>)
            (this.readers as HashMap)[field] = reader
        }
        require(reader.field == field) { "Misconfigured reader; describers do not match. This is a programmer's error." }
        return reader as DescriptorReader<D>
    }

    /**
     * Returns a cached version of the [DescriptorWriter] for the provided [Analyser].
     *
     * [Analyser] must be part of the registered schema.
     *
     * @param field The [Analyser] to return the [DescriptorWriter] for.
     * @return The [DescriptorWriter]
     */
    override fun <D : Descriptor<*>> getDescriptorWriter(field: Schema.Field<*, D>): DescriptorWriter<D> {
        var writer = this.writers[field as Schema.Field<*, Descriptor<*>>]
        if (writer == null) {
            val descriptorProvider = this.provider.obtain(field.analyser.descriptorClass)
                ?: throw IllegalArgumentException("Unhandled describer $field for provided schema.")
            writer = descriptorProvider.newWriter(this, field as Schema.Field<*, Descriptor<*>>)
            (this.writers as HashMap)[field] = writer
        }
        require(writer.field == field) { "Misconfigured writer; describers do not match. This is a programmer's error." }
        return writer as DescriptorWriter<D>
    }
}