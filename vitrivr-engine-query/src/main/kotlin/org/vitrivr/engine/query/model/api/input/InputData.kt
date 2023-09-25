package org.vitrivr.engine.query.model.api.input

import kotlinx.serialization.Serializable
import org.vitrivr.engine.core.model.content.element.ContentElement
import org.vitrivr.engine.core.model.content.element.ImageContent
import org.vitrivr.engine.core.model.content.element.TextContent
import org.vitrivr.engine.core.model.content.impl.InMemoryImageContent
import org.vitrivr.engine.core.model.content.impl.InMemoryTextContent
import org.vitrivr.engine.core.util.extension.BufferedImage
import java.awt.image.BufferedImage

@Serializable(with = InputDataSerializer::class)
sealed class InputData {
    abstract val type: InputType

    abstract fun toContent() : ContentElement<*>

}

@Serializable
data class TextInputData(val data: String) : InputData() {
    override val type = InputType.TEXT

    override fun toContent(): TextContent = InMemoryTextContent(data)

}

@Serializable
data class VectorInputData(val data: List<Float>) : InputData(){
    override val type = InputType.VECTOR

    override fun toContent(): ContentElement<*> {
        throw UnsupportedOperationException("Cannot derive content from VectorInputData")
    }

}

@Serializable
data class ImageInputData(val data: String) : InputData() {
    override val type = InputType.VECTOR
    override fun toContent(): ImageContent = InMemoryImageContent(image)

    val image: BufferedImage by lazy { BufferedImage(data) }

}
