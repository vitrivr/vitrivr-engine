package org.vitrivr.engine.query.model.api.input

import kotlinx.serialization.Serializable
import org.vitrivr.engine.core.model.content.Content
import org.vitrivr.engine.core.model.content.ImageContent
import org.vitrivr.engine.core.model.content.TextContent
import java.awt.image.BufferedImage

@Serializable(with = InputDataSerializer::class)
sealed class InputData {
    abstract val type: InputType

    abstract fun toContent() : Content<*>

}

@Serializable
data class TextInputData(val data: String) : InputData() {
    override val type = InputType.TEXT

    override fun toContent(): TextContent {
        TODO("Not yet implemented")
    }

}

@Serializable
data class VectorInputData(val data: List<Float>) : InputData(){
    override val type = InputType.VECTOR

    override fun toContent(): Content<*> {
        throw UnsupportedOperationException("Cannot derive content from VectorInputData")
    }

}

@Serializable
data class ImageInputData(val data: String) : InputData() {
    override val type = InputType.VECTOR
    override fun toContent(): ImageContent {
        TODO("Not yet implemented")
    }

    val image: BufferedImage by lazy { TODO("decode string") }

}
