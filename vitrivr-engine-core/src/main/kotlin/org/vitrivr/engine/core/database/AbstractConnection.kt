package org.vitrivr.engine.core.database

import org.vitrivr.engine.core.database.descriptor.DescriptorInitializer
import org.vitrivr.engine.core.database.descriptor.DescriptorProvider
import org.vitrivr.engine.core.database.descriptor.DescriptorReader
import org.vitrivr.engine.core.database.descriptor.DescriptorWriter
import org.vitrivr.engine.core.model.database.descriptor.Descriptor
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.operators.Describer
import kotlin.reflect.KClass

/**
 * An abstract implementation of the [Connection] interface.
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
@Suppress("UNCHECKED_CAST")
abstract class AbstractConnection(final override val schema: Schema): Connection {

    /** An internal [Map] of registered [Reader], [Writer] and [Initializer] instances. */
    private val registered = HashMap<KClass<Descriptor>, DescriptorProvider<*>>()

    /** An internal [Map] of registered [Reader], [Writer] and [Initializer] instances. */
    private val readers = HashMap<Describer<*>, DescriptorReader<*>>()

    /** An internal [Map] of registered [Reader], [Writer] and [Initializer] instances. */
    private val writers = HashMap<Describer<*>, DescriptorWriter<*>>()

    /** An internal [Map] of registered [Reader], [Writer] and [Initializer] instances. */
    private val initializers = HashMap<Describer<*>, DescriptorInitializer<*>>()

    init {
        /* Initialize the schema. */
        for (field in this.schema.fields) {
            val describer = field.newDescriber() as Describer<Descriptor>
            val provider = this.registered[describer.descriptorClass] as? DescriptorProvider<Descriptor> ?: throw IllegalArgumentException("Unhandled describer $field for provided schema.")
            this.readers[describer] = provider.newReader(describer) as DescriptorReader<*>
            this.writers[describer] = provider.newWriter(describer) as DescriptorWriter<*>
            this.initializers[describer] = provider.newInitializer(describer) as DescriptorInitializer<*>
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
     * Registers an [DescriptorProvider] for a particular [KClass] of [Descriptor] with this [Connection].
     *
     * This method is an extension point to add support for new [Descriptor]s to a pre-existing database driver.
     *
     * @param descriptorClass The [KClass] of the [Descriptor] to register [DescriptorProvider] for.
     * @param provider The [DescriptorProvider] to register.
     */
    override fun <T : Descriptor> register(descriptorClass: KClass<T>, provider: DescriptorProvider<T>) {
        require(!this.registered.containsKey(descriptorClass as KClass<Descriptor>)) { "Descriptor of class $descriptorClass cannot be registered twice."}
        this.registered[descriptorClass] = provider
    }

    /**
     * Returns a cached version of the [DescriptorInitializer] for the provided [Describer].
     *
     * [Describer] must be part of the registered schema.
     *
     * @param describer The [Describer] to return the [DescriptorInitializer] for.
     * @return The [DescriptorInitializer]
     */
    override fun <T : Descriptor> getDescriptorInitializer(describer: Describer<T>): DescriptorInitializer<T> {
        val initializer = this.initializers[describer] ?: throw IllegalArgumentException("No initializer registered for describer $describer in current schema.")
        require(initializer.describer == describer) { "Misconfigured initializer; describers do not match. This is a programmer's error." }
        return initializer as DescriptorInitializer<T>
    }

    /**
     * Returns a cached version of the [DescriptorReader] for the provided [Describer].
     *
     * [Describer] must be part of the registered schema.
     *
     * @param describer The [Describer] to return the [DescriptorReader] for.
     * @return The [DescriptorReader]
     */
    override fun <T : Descriptor> getDescriptorReader(describer: Describer<T>): DescriptorReader<T> {
        val reader = this.readers[describer] ?: throw IllegalArgumentException("No reader registered for describer $describer in current schema.")
        require(reader.describer == describer) { "Misconfigured reader; describers do not match. This is a programmer's error." }
        return reader as DescriptorReader<T>
    }

    /**
     * Returns a cached version of the [DescriptorWriter] for the provided [Describer].
     *
     * [Describer] must be part of the registered schema.
     *
     * @param describer The [Describer] to return the [DescriptorWriter] for.
     * @return The [DescriptorWriter]
     */
    override fun <T : Descriptor> getDescriptorWriter(describer: Describer<T>): DescriptorWriter<T> {
        val writer = this.writers[describer] ?: throw IllegalArgumentException("No writer registered for describer $describer in current schema.")
        require(writer.describer == describer) { "Misconfigured writer; describers do not match. This is a programmer's error." }
        return writer as DescriptorWriter<T>
    }
}