package org.vitrivr.engine.base.database.cottontail

import org.vitrivr.engine.base.database.cottontail.provider.FloatVectorDescriptorProvider
import org.vitrivr.engine.core.database.Connection
import org.vitrivr.engine.core.database.ConnectionProvider
import org.vitrivr.engine.core.database.descriptor.DescriptorProvider
import org.vitrivr.engine.core.model.database.descriptor.Descriptor
import org.vitrivr.engine.core.model.database.descriptor.vector.FloatVectorDescriptor
import org.vitrivr.engine.core.model.metamodel.Schema
import kotlin.reflect.KClass

/**
 * Implementation of the [ConnectionProvider] interface for Cottontail DB.
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
class CottontailConnectionProvider: ConnectionProvider {

    companion object {
        /** Name of the host parameter. */
        const val PARAMETER_NAME_HOST = "host"

        /** Name of the host parameter. */
        const val PARAMETER_DEFAULT_HOST = "127.0.0.1"

        /** Name of the port parameter. */
        const val PARAMETER_NAME_PORT = "port"

        /** Name of the host parameter. */
        const val PARAMETER_DEFAULT_PORT = 1865
    }

    /** The name of the database. */
    override val databaseName: String = "Cottontail DB"

    /** The version of the [CottontailConnectionProvider]. */
    override val version: String = "1.0.0"

    /** */
    private val registered = HashMap<KClass<Descriptor>, DescriptorProvider<*>>()

    init {
        /* Register all providers known to this CottontailConnection. */
        this.register(FloatVectorDescriptor::class, FloatVectorDescriptorProvider())
    }

    /**
     * Opens a new [CottontailConnection] for the given [Schema].
     *
     * @param schema [Schema] to open [Connection] for.
     * @param parameters The optional parameters.
     * @return [CottontailConnection]
     */
    override fun openConnection(schema: Schema, parameters: Map<String, String>): Connection = CottontailConnection(
        schema,
        this,
        parameters[PARAMETER_NAME_HOST] ?: PARAMETER_DEFAULT_HOST,
        parameters[PARAMETER_NAME_PORT]?.toIntOrNull() ?: PARAMETER_DEFAULT_PORT
    )

    /**
     * Registers an [DescriptorProvider] for a particular [KClass] of [Descriptor] with this [Connection].
     *
     * This method is an extension point to add support for new [Descriptor]s to a pre-existing database driver.
     *
     * @param descriptorClass The [KClass] of the [Descriptor] to register [DescriptorProvider] for.
     * @param provider The [DescriptorProvider] to register.
     */
    @Suppress("UNCHECKED_CAST")
    override fun <T : Descriptor> register(descriptorClass: KClass<T>, provider: DescriptorProvider<T>) {
        require(!this.registered.containsKey(descriptorClass as KClass<Descriptor>)) { "Descriptor of class $descriptorClass cannot be registered twice."}
        this.registered[descriptorClass] = provider
    }

    /**
     * Obtains an [DescriptorProvider] for a particular [KClass] of [Descriptor], that has been registered with this [ConnectionProvider].
     *
     * @param descriptorClass The [KClass] of the [Descriptor] to lookup [DescriptorProvider] for.
     * @return The registered [DescriptorProvider] .
     */
    @Suppress("UNCHECKED_CAST")
    override fun <T : Descriptor> obtain(descriptorClass: KClass<T>): DescriptorProvider<T>? = this.registered[descriptorClass as KClass<Descriptor>] as DescriptorProvider<T>?
}