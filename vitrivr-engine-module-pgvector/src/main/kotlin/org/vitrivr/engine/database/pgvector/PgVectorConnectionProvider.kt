package org.vitrivr.engine.database.pgvector

import org.vitrivr.engine.core.database.AbstractConnectionProvider
import org.vitrivr.engine.core.database.Connection
import org.vitrivr.engine.core.database.ConnectionProvider
import org.vitrivr.engine.core.database.descriptor.DescriptorProvider
import org.vitrivr.engine.core.model.descriptor.struct.LabelDescriptor
import org.vitrivr.engine.core.model.descriptor.struct.MapStructDescriptor
import org.vitrivr.engine.core.model.descriptor.struct.metadata.MediaDimensionsDescriptor
import org.vitrivr.engine.core.model.descriptor.struct.metadata.Rectangle2DMetadataDescriptor
import org.vitrivr.engine.core.model.descriptor.struct.metadata.TemporalMetadataDescriptor
import org.vitrivr.engine.core.model.descriptor.struct.metadata.source.FileSourceMetadataDescriptor
import org.vitrivr.engine.core.model.descriptor.struct.metadata.source.VideoSourceMetadataDescriptor
import org.vitrivr.engine.core.model.descriptor.vector.*
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.database.pgvector.descriptor.struct.StructDescriptorProvider
import org.vitrivr.engine.database.pgvector.descriptor.vector.VectorDescriptorProvider
import java.sql.DriverManager
import java.util.*


/**
 * Implementation of the [ConnectionProvider] interface for PostgreSQL with the PGVector extension.
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
class PgVectorConnectionProvider: AbstractConnectionProvider() {

    companion object {
        /** Name of the host parameter. */
        const val PARAMETER_NAME_HOST = "host"

        /** Name of the host parameter. */
        const val PARAMETER_DEFAULT_HOST = "127.0.0.1"

        /** Name of the port parameter. */
        const val PARAMETER_NAME_PORT = "port"

        /** Name of the host parameter. */
        const val PARAMETER_DEFAULT_PORT = 5432

        /** Name of the host parameter. */
        const val PARAMETER_NAME_USERNAME = "username"

        /** Name of the host parameter. */
        const val PARAMETER_NAME_PASSWORD = "password"

        /** Name of the host parameter. */
        const val PARAMETER_NAME_SSL = "ssl"
    }

    /** The name of this [PgVectorConnectionProvider]. */
    override val databaseName: String = "PostgreSQL with pgVector"

    /** The version of this [PgVectorConnectionProvider]. */
    override val version: String = "1.0.0"

    /**
     * This method is called during initialization of the [PgVectorConnectionProvider] and can be used to register [DescriptorProvider]s.
     */
    override fun initialize() {
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
        this.register(MapStructDescriptor::class, StructDescriptorProvider)
    }

    /**
     * Opens a new [PgVectorConnection] for the given [Schema].
     *
     * @param parameters The optional parameters.
     * @return [Connection]
     */
    override fun openConnection(schemaName: String, parameters: Map<String, String>): Connection {

        /* Prepare connection URL. */
        val host = parameters.getOrDefault(PARAMETER_NAME_HOST, PARAMETER_DEFAULT_HOST)
        val port = parameters[PARAMETER_NAME_PORT]?.toInt() ?: PARAMETER_DEFAULT_PORT
        val url = "jdbc:postgresql://${host}:${port}/"

        /* Prepare properties (optional). */
        val props = Properties()
        parameters[PARAMETER_NAME_USERNAME]?.let { props.setProperty("user", it) }
        parameters[PARAMETER_NAME_PASSWORD]?.let { props.setProperty("password", it) }
        parameters[PARAMETER_NAME_SSL]?.let { props.setProperty("ssl", it) }

        /* Open JDBC connection and return PgVectorConnection. */
        return PgVectorConnection(this, schemaName, DriverManager.getConnection(url, props))
    }
}