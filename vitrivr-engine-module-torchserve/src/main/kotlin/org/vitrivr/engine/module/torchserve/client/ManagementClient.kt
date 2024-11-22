package org.vitrivr.engine.module.torchserve.client

import com.google.auth.oauth2.AccessToken
import com.google.auth.oauth2.OAuth2Credentials
import io.grpc.Grpc
import io.grpc.InsecureChannelCredentials
import io.grpc.ManagedChannel
import io.grpc.auth.MoreCallCredentials
import org.pytorch.serve.grpc.management.ManagementAPIsServiceGrpc
import org.pytorch.serve.grpc.management.describeModelRequest
import org.pytorch.serve.grpc.management.listModelsRequest
import java.util.*

class ManagementClient(private val host: String, private val port: Int = 7071, private val token: String? = null) {

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
    private lateinit var stub: ManagementAPIsServiceGrpc.ManagementAPIsServiceBlockingStub
    fun connect() {

        this.channel = Grpc.newChannelBuilderForAddress(host, port, InsecureChannelCredentials.create()).build()
        this.stub = ManagementAPIsServiceGrpc.newBlockingStub(channel)

    }


    fun listModels(): String { //TODO do some parsing?

        val response = this.stub.let {
            if (credentials != null) {
                it.withCallCredentials(this.credentials)
            } else {
                it
            }
        }.listModels(
            listModelsRequest { }
        )
        return response.msg

    }

    fun describeModel(name: String, version: String? = null): String {

        return this.stub.let {
            if (credentials != null) {
                it.withCallCredentials(this.credentials)
            } else {
                it
            }
        }.describeModel(
            describeModelRequest {
                modelName = name
                if (version != null) {
                    modelVersion = version
                }
            }
        ).msg

    }

}