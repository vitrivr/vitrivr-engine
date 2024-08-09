package org.vitrivr.engine.module.torchserve.client

import com.google.auth.oauth2.AccessToken
import com.google.auth.oauth2.OAuth2Credentials
import com.google.protobuf.ByteString
import io.grpc.Grpc
import io.grpc.InsecureChannelCredentials
import io.grpc.ManagedChannel
import io.grpc.auth.MoreCallCredentials
import org.pytorch.serve.grpc.inference.InferenceAPIsServiceGrpc
import org.pytorch.serve.grpc.inference.predictionsRequest
import java.util.*

class InferenceClient(private val host: String, private val port: Int = 7070, private val token: String? = null) {

    private val credentials = token?.let {
        MoreCallCredentials.from(
            OAuth2Credentials.create(
                AccessToken(
                    "Bearer: $it",
                    Date(Long.MAX_VALUE)
                )
            )
        )
    }
    private lateinit var channel: ManagedChannel
    private lateinit var blockingStub: InferenceAPIsServiceGrpc.InferenceAPIsServiceBlockingStub

    fun connect() {
        this.channel = Grpc.newChannelBuilderForAddress(host, port, InsecureChannelCredentials.create()).build()
        this.blockingStub = InferenceAPIsServiceGrpc.newBlockingStub(channel)
    }

    fun predict(modelName: String, input: Map<String, ByteString>, modelVersion: String? = null) : ByteString {

        val response = blockingStub.let {
            if (credentials != null) {
                it.withCallCredentials(this.credentials)
            } else {
                it
            }
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