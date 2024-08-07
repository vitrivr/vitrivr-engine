package org.vitrivr.engine.plugin.cottontaildb

import org.vitrivr.engine.core.database.AbstractConnectionProvider
import org.vitrivr.engine.core.database.Connection
import org.vitrivr.engine.core.database.ConnectionProvider
import org.vitrivr.engine.core.database.descriptor.DescriptorProvider
import org.vitrivr.engine.core.model.descriptor.scalar.*
import org.vitrivr.engine.core.model.descriptor.struct.LabelDescriptor
import org.vitrivr.engine.core.model.descriptor.struct.metadata.MediaDimensionsDescriptor
import org.vitrivr.engine.core.model.descriptor.struct.metadata.Rectangle2DMetadataDescriptor
import org.vitrivr.engine.core.model.descriptor.struct.metadata.TemporalMetadataDescriptor
import org.vitrivr.engine.core.model.descriptor.struct.metadata.source.FileSourceMetadataDescriptor
import org.vitrivr.engine.core.model.descriptor.struct.metadata.source.VideoSourceMetadataDescriptor
import org.vitrivr.engine.core.model.descriptor.vector.*
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.plugin.cottontaildb.descriptors.scalar.ScalarDescriptorProvider
import org.vitrivr.engine.plugin.cottontaildb.descriptors.struct.StructDescriptorProvider
import org.vitrivr.engine.plugin.cottontaildb.descriptors.vector.VectorDescriptorProvider

/**
 * Implementation of the [ConnectionProvider] interface for Cottontail DB.
 *
 * @author Ralph Gasser
 * @version 1.1.0
 */
class CottontailConnectionProvider: AbstractConnectionProvider() {

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

    /**
     * This method is called during initialization of the [CottontailConnectionProvider] and can be used to register [DescriptorProvider]s.
     */
    override fun initialize() {
        /* Scalar descriptors. */
        this.register(BooleanDescriptor::class, ScalarDescriptorProvider)
        this.register(IntDescriptor::class, ScalarDescriptorProvider)
        this.register(LongDescriptor::class, ScalarDescriptorProvider)
        this.register(FloatDescriptor::class, ScalarDescriptorProvider)
        this.register(DoubleDescriptor::class, ScalarDescriptorProvider)
        this.register(StringDescriptor::class, ScalarDescriptorProvider)
        this.register(TextDescriptor::class, ScalarDescriptorProvider)

        /* Vector descriptors. */
        this.register(BooleanVectorDescriptor::class, VectorDescriptorProvider)
        this.register(IntVectorDescriptor::class, VectorDescriptorProvider)
        this.register(LongVectorDescriptor::class, VectorDescriptorProvider)
        this.register(FloatVectorDescriptor::class, VectorDescriptorProvider)
        this.register(DoubleVectorDescriptor::class, VectorDescriptorProvider)

        /* Struct descriptor. */
        this.register(LabelDescriptor::class, StructDescriptorProvider)
        this.register(FileSourceMetadataDescriptor::class, StructDescriptorProvider)
        this.register(VideoSourceMetadataDescriptor::class, StructDescriptorProvider)
        this.register(TemporalMetadataDescriptor::class, StructDescriptorProvider)
        this.register(Rectangle2DMetadataDescriptor::class, StructDescriptorProvider)
        this.register(MediaDimensionsDescriptor::class, StructDescriptorProvider)
    }

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
}