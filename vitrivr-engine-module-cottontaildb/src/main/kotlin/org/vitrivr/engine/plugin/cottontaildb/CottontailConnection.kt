package org.vitrivr.engine.plugin.cottontaildb

import io.grpc.ManagedChannel
import io.grpc.ManagedChannelBuilder
import org.vitrivr.cottontail.client.SimpleClient
import org.vitrivr.engine.core.database.AbstractConnection
import org.vitrivr.engine.plugin.cottontaildb.retrievable.RetrievableInitializer
import org.vitrivr.engine.plugin.cottontaildb.retrievable.RetrievableReader
import org.vitrivr.engine.plugin.cottontaildb.retrievable.RetrievableWriter

/**
 * A [AbstractConnection] to connect to a Cottontail DB instance.
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
class CottontailConnection(provider: CottontailConnectionProvider, schemaName: String, private val host: String, private val port: Int) : AbstractConnection(schemaName, provider) {

    /** The [ManagedChannel] instance used by this [CottontailConnection]. */
    private val channel = ManagedChannelBuilder.forAddress(this.host, this.port).usePlaintext().build()

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