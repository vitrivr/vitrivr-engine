package org.vitrivr.engine.query.model.api.input

import kotlinx.serialization.Serializable
import java.awt.image.BufferedImage

@Serializable(with = InputDataSerializer::class)
sealed class InputData {
    abstract val type: InputType
}

@Serializable
data class TextInputData(val data: String) : InputData() {
    override val type = InputType.TEXT
}

@Serializable
data class VectorInputData(val data: List<Float>) : InputData(){
    override val type = InputType.VECTOR
}

@Serializable
data class ImageInputData(val data: String) : InputData() {
    override val type = InputType.VECTOR

    val image: BufferedImage by lazy { TODO("decode string") }

}
