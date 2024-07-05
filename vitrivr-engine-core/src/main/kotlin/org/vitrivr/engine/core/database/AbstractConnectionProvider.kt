package org.vitrivr.engine.core.database

import org.vitrivr.engine.core.database.descriptor.DescriptorProvider
import org.vitrivr.engine.core.model.descriptor.Descriptor
import kotlin.reflect.KClass

/**
 * Abstract implementation of the [ConnectionProvider] interface, which provides basic facilities to register [DescriptorProvider]s.
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
abstract class AbstractConnectionProvider: ConnectionProvider {
    /** A map of registered [DescriptorProvider]. */
    protected val registered: MutableMap<KClass<*>, DescriptorProvider<*>> = HashMap()


    init {
        this.initialize()
    }

    /**
     * This method is called during initialization of the [AbstractConnectionProvider] and can be used to register [DescriptorProvider]s.
     */
    abstract fun initialize()

    /**
     * Registers an [DescriptorProvider] for a particular [KClass] of [Descriptor] with this [Connection].
     *
     * This method is an extension point to add support for new [Descriptor]s to a pre-existing database driver.
     *
     * @param descriptorClass The [KClass] of the [Descriptor] to register [DescriptorProvider] for.
     * @param provider The [DescriptorProvider] to register.
     */
    override fun <T : Descriptor> register(descriptorClass: KClass<T>, provider: DescriptorProvider<*>) {
        this.registered[descriptorClass] = provider
    }

    /**
     * Obtains an [DescriptorProvider] for a particular [KClass] of [Descriptor], that has been registered with this [ConnectionProvider].
     *
     * @param descriptorClass The [KClass] of the [Descriptor] to lookup [DescriptorProvider] for.
     * @return The registered [DescriptorProvider] .
     */
    @Suppress("UNCHECKED_CAST")
    override fun <T : Descriptor> obtain(descriptorClass: KClass<T>): DescriptorProvider<T> {
        val provider = this.registered[descriptorClass] ?: throw IllegalStateException("No DescriptorProvider registered for $descriptorClass.")
        return provider as DescriptorProvider<T>
    }
}