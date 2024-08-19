package org.vitrivr.engine.core.database.blackhole

import org.vitrivr.engine.core.database.ConnectionProvider
import org.vitrivr.engine.core.database.blackhole.descriptors.BlackholeDescriptionProvider
import org.vitrivr.engine.core.database.descriptor.DescriptorProvider
import org.vitrivr.engine.core.model.descriptor.Descriptor
import kotlin.reflect.KClass

/**
 * A [ConnectionProvider] for the [BlackholeConnection].
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
class BlackholeConnectionProvider(override val databaseName: String, override val version: String = "1.0.0") : ConnectionProvider {
    override fun openConnection(schemaName: String, parameters: Map<String, String>) = BlackholeConnection(schemaName, this, parameters["log"]?.toBoolean() == true)
    override fun <T : Descriptor<*>> register(descriptorClass: KClass<T>, provider: DescriptorProvider<*>) { /* No op. */ }
    override fun <T : Descriptor<*>> obtain(descriptorClass: KClass<T>) = BlackholeDescriptionProvider<T>()
}
