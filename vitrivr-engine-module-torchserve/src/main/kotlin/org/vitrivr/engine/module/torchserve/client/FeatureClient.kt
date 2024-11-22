package org.vitrivr.engine.module.torchserve.client

import com.google.protobuf.ByteString

abstract class FeatureClient<I, O>(private val inferenceClient: InferenceClient, private val modelName: String, private val modelVersion: String? = null) {

    abstract fun encode(input: I): Map<String, ByteString>
    abstract fun decode(bytes: ByteString): O

    fun extract(input: I): O = decode(
        inferenceClient.predict(
            modelName,
            encode(input),
            modelVersion
        )
    )

}