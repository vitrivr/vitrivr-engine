package org.vitrivr.engine.module.torchserve.client

import com.google.protobuf.ByteString
import org.pytorch.serve.grpc.inference.InferenceAPIsServiceGrpc
import org.pytorch.serve.grpc.inference.predictionsRequest


/**
 * A client for connecting to a TorchServe instance and sending inference requests.
 *
 * @author Luca Rossetto
 * @author Ralph Gasser
 * @version 1.0.3
 */
class InferenceClient(host: String, port: Int = 8080, token: String? = null) : AbstractTorchServeClient(host, port, token) {

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
}