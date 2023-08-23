package org.vitrivr.engine.core.database

import org.vitrivr.engine.core.database.descriptor.DescriptorProvider
import org.vitrivr.engine.core.model.database.descriptor.Descriptor
import org.vitrivr.engine.core.model.metamodel.Schema
import java.util.ServiceLoader
import kotlin.reflect.KClass

/**
 * A provider class for a database [Connection].
 *
 * This is the entry point for the Java [ServiceLoader] interface. To provide your own implementation,
 * add the FQN of your [ConnectionProvider] to the file META-INF/services/org.vitrivr.engine.core.database.ConnectionProvider
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
interface ConnectionProvider {

    /** The name of the database (system) this [ConnectionProvider] provides a [Connection] to. */
    val databaseName: String

    /** The version of this [ConnectionProvider]. */
    val version: String

    /**
     * Opens a new [Connection] for the given [Schema].
     *
     * @param schemaName The name of the schema.
     * @param parameters The optional parameters.
     * @return [Connection]
     */
    fun openConnection(schemaName: String, parameters: Map<String,String> = emptyMap()): Connection

    /**
     * Registers an [DescriptorProvider] for a particular [KClass] of [Descriptor] with this [ConnectionProvider].
     *
     * This method is an extension point to add support for new [Descriptor]s to a pre-existing database driver.
     *
     * @param descriptorClass The [KClass] of the [Descriptor] to register [DescriptorProvider] for.
     * @param provider The [DescriptorProvider] to register.
     */
    fun <T : Descriptor> register(descriptorClass: KClass<T>, provider: DescriptorProvider<T>)

    /**
     * Obtains an [DescriptorProvider] for a particular [KClass] of [Descriptor], that has been registered with this [ConnectionProvider].
     *
     * @param descriptorClass The [KClass] of the [Descriptor] to lookup [DescriptorProvider] for.
     * @return The registered [DescriptorProvider] .
     */
    fun <T : Descriptor> obtain(descriptorClass: KClass<T>): DescriptorProvider<T>?
}