package org.vitrivr.engine.module.torchserve.client

import org.pytorch.serve.grpc.management.ManagementAPIsServiceGrpc
import org.pytorch.serve.grpc.management.describeModelRequest
import org.pytorch.serve.grpc.management.listModelsRequest

/**
 * A client for connecting to a TorchServe instance and sending management requests via gRPC.
 *
 * @author Luca Rossetto
 * @author Ralph Gasser
 * @version 1.0.3
 */
class ManagementClient(host: String, port: Int = 7071, token: String? = null) : AbstractTorchServeClient(host, port, token) {

    private lateinit var stub: ManagementAPIsServiceGrpc.ManagementAPIsServiceBlockingStub


    /**
     *
     */
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

    /**
     *
     */
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