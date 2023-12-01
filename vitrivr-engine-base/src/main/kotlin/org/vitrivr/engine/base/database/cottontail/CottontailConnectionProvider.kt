package org.vitrivr.engine.base.database.cottontail

import org.vitrivr.engine.base.database.cottontail.descriptors.label.StructDescriptorProvider
import org.vitrivr.engine.base.database.cottontail.descriptors.scalar.ScalarDescriptorProvider
import org.vitrivr.engine.core.database.Connection
import org.vitrivr.engine.core.database.ConnectionProvider
import org.vitrivr.engine.core.database.descriptor.DescriptorProvider
import org.vitrivr.engine.core.model.descriptor.Descriptor
import org.vitrivr.engine.core.model.descriptor.scalar.*
import org.vitrivr.engine.core.model.descriptor.struct.LabelDescriptor
import org.vitrivr.engine.core.model.descriptor.struct.RasterDescriptor
import org.vitrivr.engine.core.model.descriptor.struct.SkeletonDescriptor
import org.vitrivr.engine.core.model.descriptor.struct.metadata.FileMetadataDescriptor
import org.vitrivr.engine.core.model.descriptor.struct.metadata.MediaDimensionsDescriptor
import org.vitrivr.engine.core.model.descriptor.struct.metadata.Rectangle2DMetadataDescriptor
import org.vitrivr.engine.core.model.descriptor.struct.metadata.TemporalMetadataDescriptor
import org.vitrivr.engine.core.model.descriptor.vector.*
import org.vitrivr.engine.core.model.metamodel.Schema
import java.util.*
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

    /** List of registered [LinkedList]*/
    private val registered = mutableMapOf<KClass<*>, DescriptorProvider<*>>(
        /* Scalar descriptors. */
        BooleanDescriptor::class to ScalarDescriptorProvider,
        IntDescriptor::class to ScalarDescriptorProvider,
        LongDescriptor::class to ScalarDescriptorProvider,
        FloatDescriptor::class to ScalarDescriptorProvider,
        DoubleDescriptor::class to ScalarDescriptorProvider,
        StringDescriptor::class to ScalarDescriptorProvider,

        /* Vector descriptors. */
        BooleanVectorDescriptor::class to org.vitrivr.engine.base.database.cottontail.descriptors.vector.VectorDescriptorProvider,
        IntVectorDescriptor::class to org.vitrivr.engine.base.database.cottontail.descriptors.vector.VectorDescriptorProvider,
        LongVectorDescriptor::class to org.vitrivr.engine.base.database.cottontail.descriptors.vector.VectorDescriptorProvider,
        FloatVectorDescriptor::class to org.vitrivr.engine.base.database.cottontail.descriptors.vector.VectorDescriptorProvider,
        DoubleVectorDescriptor::class to org.vitrivr.engine.base.database.cottontail.descriptors.vector.VectorDescriptorProvider,

        /* Struct descriptor. */
        LabelDescriptor::class to StructDescriptorProvider,
        FileMetadataDescriptor::class to StructDescriptorProvider,
        TemporalMetadataDescriptor::class to StructDescriptorProvider,
        Rectangle2DMetadataDescriptor::class to StructDescriptorProvider,
            MediaDimensionsDescriptor::class to StructDescriptorProvider,
            SkeletonDescriptor::class to StructDescriptorProvider,
            RasterDescriptor::class to StructDescriptorProvider
    )

    /**
     * Opens a new [CottontailConnection] for the given [Schema].
     *
     * @param parameters The optional parameters.
     * @return [CottontailConnection]
     */
    override fun openConnection(schemaName: String, parameters: Map<String, String>): Connection = CottontailConnection(
        this,
        schemaName,
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
    override fun <T : Descriptor> register(descriptorClass: KClass<T>, provider: DescriptorProvider<T>) {
        this.registered[descriptorClass] = provider
    }

    /**
     * Obtains an [DescriptorProvider] for a particular [KClass] of [Descriptor], that has been registered with this [ConnectionProvider].
     *
     * @param descriptorClass The [KClass] of the [Descriptor] to lookup [DescriptorProvider] for.
     * @return The registered [DescriptorProvider] .
     */
    @Suppress("UNCHECKED_CAST")
    override fun <T : Descriptor> obtain(descriptorClass: KClass<T>): DescriptorProvider<T> = this.registered[descriptorClass] as DescriptorProvider<T>
}