package org.vitrivr.engine.module.torchserve.client

import com.google.auth.oauth2.AccessToken
import com.google.auth.oauth2.OAuth2Credentials
import io.grpc.Grpc
import io.grpc.InsecureChannelCredentials
import io.grpc.ManagedChannel
import io.grpc.auth.MoreCallCredentials
import java.io.Closeable
import java.util.*

/**
 * An abstract client for connecting to a TorchServe instance via gRPC
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
abstract class AbstractTorchServeClient(val host: String, val port: Int = 8080, private val token: String? = null) : Closeable {

    /** Credentials used for connecting to TorchServe. */
    protected val credentials = this.token?.let {
        MoreCallCredentials.from(OAuth2Credentials.create(AccessToken(it, Date(Long.MAX_VALUE))))
    }

    /** The [ManagedChannel] used for gRPC communication. */
    protected val channel: ManagedChannel by lazy { Grpc.newChannelBuilderForAddress(this.host, this.port, InsecureChannelCredentials.create()).build() }

    /**
     * Closes this [AbstractTorchServeClient].
     */
    override fun close() {
        this.channel.shutdownNow()
    }
}