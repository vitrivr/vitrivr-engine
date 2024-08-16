package org.vitrivr.engine.core.database

import org.vitrivr.engine.core.database.descriptor.DescriptorProvider
import org.vitrivr.engine.core.model.descriptor.Descriptor
import kotlin.reflect.KClass

class MockConnectionProvider(override val databaseName: String, override val version: String = "TEST-1") : ConnectionProvider {
    override fun openConnection(schemaName: String, parameters: Map<String, String>): Connection {
        TODO("Not yet implemented")
    }

    override fun <T : Descriptor<*>> register(
        descriptorClass: KClass<T>,
        provider: DescriptorProvider<*>
    ) {
        TODO("Not yet implemented")
    }

    override fun <T : Descriptor<*>> obtain(descriptorClass: KClass<T>): DescriptorProvider<T>? {
        TODO("Not yet implemented")
    }
}
