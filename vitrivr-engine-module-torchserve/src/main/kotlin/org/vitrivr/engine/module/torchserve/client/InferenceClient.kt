package org.vitrivr.engine.module.torchserve.client

import com.google.auth.oauth2.AccessToken
import com.google.auth.oauth2.OAuth2Credentials
import com.google.protobuf.ByteString
import io.grpc.Grpc
import io.grpc.InsecureChannelCredentials
import io.grpc.auth.MoreCallCredentials
import org.pytorch.serve.grpc.inference.InferenceAPIsServiceGrpc
import org.pytorch.serve.grpc.inference.predictionsRequest
import java.io.Closeable
import java.util.*


/**
 * A client for connecting to a TorchServe instance and sending inference requests.
 *
 * @author Luca Rossetto
 * @author Ralph Gasser
 * @version 1.0.03
 */
class InferenceClient(val host: String, val port: Int = 8080, private val token: String? = null) : Closeable {

    /** Credentials used for connecting to TorchServe. */
    private val credentials = this.token?.let {
        MoreCallCredentials.from(OAuth2Credentials.create(AccessToken(it, Date(Long.MAX_VALUE))))
    }

    /** */
    private val channel by lazy { Grpc.newChannelBuilderForAddress(this.host, this.port, InsecureChannelCredentials.create()).build() }

    /** The stub used to communicate with TorchServe. */
    private val blockingStub by lazy { InferenceAPIsServiceGrpc.newBlockingStub(this.channel) }

    /**
     * Sends an inference request to the TorchServe instance.
     *
     *  @param modelName Name of the model to be used for inference.
     *  @param input Input to the model.
     *  @param modelVersion Version of the model to be used for inference.
     */
    fun predict(modelName: String, input: Map<String, ByteString>, modelVersion: String? = null): ByteString {
        val response = if (credentials != null) {
            this.blockingStub.withCallCredentials(this.credentials)
        } else {
            this.blockingStub
        }.predictions(
            predictionsRequest {
                this.modelName = modelName
                this.input.putAll(input)
                if (modelVersion != null) {
                    this.modelVersion = modelVersion
                }
            }
        )
        return response.prediction
    }

    /**
     * Closes this [InferenceClient].
     */
    override fun close() {
        this.channel.shutdownNow()
    }
}