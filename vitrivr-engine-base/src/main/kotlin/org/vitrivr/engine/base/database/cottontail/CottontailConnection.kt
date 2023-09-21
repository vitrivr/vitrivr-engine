package org.vitrivr.engine.base.database.cottontail

import io.grpc.ManagedChannel
import io.grpc.ManagedChannelBuilder
import org.vitrivr.cottontail.client.SimpleClient
import org.vitrivr.engine.base.database.cottontail.retrievable.RetrievableInitializer
import org.vitrivr.engine.base.database.cottontail.retrievable.RetrievableReader
import org.vitrivr.engine.base.database.cottontail.retrievable.RetrievableWriter
import org.vitrivr.engine.core.database.AbstractConnection

/**
 * A [AbstractConnection] to connect to a Cottontail DB instance.
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
class CottontailConnection(provider: CottontailConnectionProvider, schemaName: String, val host: String, val port: Int): AbstractConnection(schemaName, provider) {

    companion object {
        /** The name of the retrievable entity. */
        const val RETRIEVABLE_ENTITY_NAME = "retrievable"

        /** The column name of a retrievable ID. */
        const val RETRIEVABLE_ID_COLUMN_NAME = "retrievableId"

        /** The column name of a retrievable ID. */
        const val RETRIEVABLE_TYPE_COLUMN_NAME = "type"

        /** The prefix for descriptor entities. */
        const val DESCRIPTOR_ENTITY_PREFIX = "descriptor"

        /** The column name of a descriptor ID. */
        const val DESCRIPTOR_ID_COLUMN_NAME = "descriptorId"
    }

    /** The [ManagedChannel] instance used by this [CottontailConnection]. */
    internal val channel = ManagedChannelBuilder.forAddress(this.host, this.port).usePlaintext().build()

    /** The [SimpleClient] instance used by this [CottontailConnection]. */
    internal val client = SimpleClient(this.channel)

    /**
     * Generates and returns a [RetrievableInitializer] for this [CottontailConnection].
     *
     * @return [RetrievableInitializer]
     */
    override fun getRetrievableInitializer(): org.vitrivr.engine.core.database.retrievable.RetrievableInitializer = RetrievableInitializer(this)

    /**
     * Generates and returns a [RetrievableWriter] for this [CottontailConnection].
     *
     * @return [RetrievableWriter]
     */
    override fun getRetrievableWriter(): org.vitrivr.engine.core.database.retrievable.RetrievableWriter = RetrievableWriter(this)

    /**
     * Generates and returns a [RetrievableWriter] for this [CottontailConnection].
     *
     * @return [RetrievableReader]
     */
    override fun getRetrievableReader(): org.vitrivr.engine.core.database.retrievable.RetrievableReader = RetrievableReader(this)

    /**
     * Returns the human-readable description of this [CottontailConnection].
     *
     * @return Description
     */
    override fun description(): String = "cottontaildb://${this.host}:${this.port}"

    /**
     * Closes this [CottontailConnection]
     */
    override fun close() {
        this.channel.shutdown()
    }
}