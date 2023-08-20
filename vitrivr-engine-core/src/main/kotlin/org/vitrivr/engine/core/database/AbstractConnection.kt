package org.vitrivr.engine.core.database

import org.vitrivr.engine.core.database.descriptor.DescriptorInitializer
import org.vitrivr.engine.core.database.descriptor.DescriptorProvider
import org.vitrivr.engine.core.database.descriptor.DescriptorReader
import org.vitrivr.engine.core.database.descriptor.DescriptorWriter
import org.vitrivr.engine.core.model.database.descriptor.Descriptor
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.metamodel.Analyser
import org.vitrivr.engine.core.model.metamodel.Field
import org.vitrivr.engine.core.model.metamodel.SchemaManager

/**
 * An abstract implementation of the [Connection] interface.
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
@Suppress("UNCHECKED_CAST")
abstract class AbstractConnection(final override val schema: Schema, provider: ConnectionProvider): Connection {

    /** An internal [Map] of registered [Reader], [Writer] and [Initializer] instances. */
    private val readers by lazy {
        this.schema.fields.associate { field ->
            val describer = SchemaManager.getAnalyserForName(field.analyserName)
            val descriptorProvider = provider.obtain(describer.descriptorClass) as? DescriptorProvider<Descriptor>
                ?: throw IllegalArgumentException("Unhandled describer $field for provided schema.")
            field to descriptorProvider.newReader(this, field as Field<Descriptor>) as DescriptorReader<*>
        }
    }

    /** An internal [Map] of registered [Reader], [Writer] and [Initializer] instances. */
    private val writers by lazy {
        this.schema.fields.associate { field ->
            val describer = SchemaManager.getAnalyserForName(field.analyserName)
            val descriptorProvider = provider.obtain(describer.descriptorClass) as? DescriptorProvider<Descriptor>
                ?: throw IllegalArgumentException("Unhandled describer $field for provided schema.")
            field to descriptorProvider.newWriter(this, field as Field<Descriptor>) as DescriptorWriter<*>
        }
    }

    /** An internal [Map] of registered [Reader], [Writer] and [Initializer] instances. */
    private val initializers by lazy {
        this.schema.fields.associate { field ->
            val describer = SchemaManager.getAnalyserForName(field.analyserName)
            val descriptorProvider = provider.obtain(describer.descriptorClass) as? DescriptorProvider<Descriptor>
                ?: throw IllegalArgumentException("Unhandled describer $field for provided schema.")
            field to descriptorProvider.newInitializer(this, field as Field<Descriptor>) as DescriptorInitializer<*>
        }
    }

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
    override fun <T : Descriptor> getDescriptorInitializer(field: Field<T>): DescriptorInitializer<T> {
        val initializer = this.initializers[field] ?: throw IllegalArgumentException("No initializer registered for describer $field in current schema.")
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
    override fun <T : Descriptor> getDescriptorReader(field: Field<T>): DescriptorReader<T> {
        val reader = this.readers[field] ?: throw IllegalArgumentException("No reader registered for describer $field in current schema.")
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
    override fun <T : Descriptor> getDescriptorWriter(field: Field<T>): DescriptorWriter<T> {
        val writer = this.writers[field] ?: throw IllegalArgumentException("No writer registered for describer $field in current schema.")
        require(writer.field == field) { "Misconfigured writer; describers do not match. This is a programmer's error." }
        return writer as DescriptorWriter<T>
    }
}