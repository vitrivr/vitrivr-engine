package org.vitrivr.engine.base.database.cottontail

import io.grpc.ManagedChannel
import io.grpc.ManagedChannelBuilder
import org.vitrivr.cottontail.client.SimpleClient
import org.vitrivr.cottontail.core.database.Name
import org.vitrivr.engine.base.database.cottontail.initializer.RetrievableInitializer
import org.vitrivr.engine.base.database.cottontail.reader.RetrievableReader
import org.vitrivr.engine.base.database.cottontail.writer.RetrievableWriter
import org.vitrivr.engine.core.database.*
import org.vitrivr.engine.core.model.metamodel.Schema

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

        /** The prefix for descriptor entities. */
        const val DESCRIPTOR_ENTITY_PREFIX = "descriptor"
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
     * Closes this [CottontailConnection]
     */
    override fun close() {
        this.channel.shutdown()
    }
}