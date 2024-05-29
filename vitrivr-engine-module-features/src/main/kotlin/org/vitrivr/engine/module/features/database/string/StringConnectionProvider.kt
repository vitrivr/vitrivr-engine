package org.vitrivr.engine.module.features.database.string

import org.vitrivr.engine.core.database.ConnectionProvider
import org.vitrivr.engine.core.database.descriptor.DescriptorProvider
import org.vitrivr.engine.core.model.Persistable
import org.vitrivr.engine.core.model.descriptor.Descriptor
import org.vitrivr.engine.core.model.descriptor.scalar.StringDescriptor
import org.vitrivr.engine.core.model.descriptor.vector.FloatVectorDescriptor
import java.io.OutputStream
import kotlin.reflect.KClass

class StringConnectionProvider(internal val targetStream: OutputStream = System.out, val stringify: (Persistable) -> String = { persistable -> persistable.toString() }) : ConnectionProvider {

    override val databaseName = "String"
    override val version = "1.0"

    private val registered = HashMap<KClass<Descriptor>, DescriptorProvider<*>>()

    init {
        /* Register all providers known to this CottontailConnection. */
        this.register(FloatVectorDescriptor::class, StringWriterProvider<FloatVectorDescriptor>())
        this.register(StringDescriptor::class, StringWriterProvider<StringDescriptor>())

    }

    override fun openConnection(schemaName: String, parameters: Map<String, String>): StringConnection = StringConnection(this, schemaName, stringify)

    @Suppress("UNCHECKED_CAST")
    override fun <T : Descriptor> register(descriptorClass: KClass<T>, provider: DescriptorProvider<*>) {
        require(!this.registered.containsKey(descriptorClass as KClass<Descriptor>)) { "Descriptor of class $descriptorClass cannot be registered twice."}
        this.registered[descriptorClass] = provider
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : Descriptor> obtain(descriptorClass: KClass<T>): DescriptorProvider<T>? = this.registered[descriptorClass as KClass<Descriptor>] as DescriptorProvider<T>?
}