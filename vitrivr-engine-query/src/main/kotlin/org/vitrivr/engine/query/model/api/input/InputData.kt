package org.vitrivr.engine.query.model.api.input

import io.javalin.openapi.Discriminator
import io.javalin.openapi.DiscriminatorProperty
import io.javalin.openapi.OneOf
import kotlinx.serialization.Serializable
import org.vitrivr.engine.core.model.content.element.ContentElement
import org.vitrivr.engine.core.model.content.element.DescriptorContent
import org.vitrivr.engine.core.model.content.element.IdContent
import org.vitrivr.engine.core.model.content.element.ImageContent
import org.vitrivr.engine.core.model.content.element.TextContent
import org.vitrivr.engine.core.model.content.impl.memory.InMemoryImageContent
import org.vitrivr.engine.core.model.content.impl.memory.InMemoryTextContent
import org.vitrivr.engine.core.model.descriptor.scalar.BooleanDescriptor
import org.vitrivr.engine.core.model.descriptor.scalar.DoubleDescriptor
import org.vitrivr.engine.core.model.descriptor.scalar.TextDescriptor
import org.vitrivr.engine.core.model.descriptor.vector.FloatVectorDescriptor
import org.vitrivr.engine.core.util.extension.BufferedImage
import java.text.SimpleDateFormat
import java.util.*

/**
 * The abstract [InputData], essentially a query's input value.
 */
@Serializable(with = InputDataSerializer::class)
@OneOf(
    discriminator = Discriminator(DiscriminatorProperty("type", type = InputType::class)),
    value = [
        TextInputData::class,
        ImageInputData::class,
        VectorInputData::class,
        RetrievableIdInputData::class,
        BooleanInputData::class,
        NumericInputData::class,
        DateInputData::class
    ]
)
sealed class InputData() {
    /**
     * The [InputType] of this [InputType]. Required for polymorphic deserialisation.
     */
    abstract val type: InputType

    /**
     * Converts the given [InputData] to a [ContentElement] if supported.
     *
     * @throws UnsupportedOperationException If there is no way to convert the input to a content
     */
    abstract fun toContent() : ContentElement<*>

}

/**
 * [InputData] for textual input.
 * Can be converted to a [ContentElement], specifically a [TextContent].
 */
@Serializable
data class TextInputData(val data: String) : InputData() {
    override val type = InputType.TEXT
    override fun toContent(): DescriptorContent<TextDescriptor> = InMemoryTextContent(data)
}

/**
 * [InputData] for vector input.
 * Cannot be converted to a [ContentElement]
 */
@Serializable
data class VectorInputData(val data: List<Float>) : InputData(){
    override val type = InputType.VECTOR
    override fun toContent(): DescriptorContent<FloatVectorDescriptor> = TODO()
}

/**
 * [InputData] for image input in base64 format.
 * Can be converted to a [ContentElement], specifically to a [InMemoryImageContent].
 */
@Serializable
data class ImageInputData(val data: String) : InputData() {
    override val type = InputType.IMAGE
    override fun toContent(): ImageContent = InMemoryImageContent(BufferedImage(data))
}

/**
 * [InputData] for a retrievable id.
 * Cannot be converted to a [ContentElement]
 */
@Serializable
data class RetrievableIdInputData(val id: String) : InputData() {

    override val type = InputType.ID

    override fun toContent(): IdContent = IdContent(UUID.fromString(id))

}

/**
 * [InputData] for boolean input.
 * Cannot be converted to a [ContentElement]
 */
@Serializable
data class BooleanInputData(val data: Boolean): InputData(){
    override val type = InputType.BOOLEAN
    override fun toContent(): DescriptorContent<BooleanDescriptor> = TODO()
}

/**
 * [InputData] for numeric input.
 * Cannot be converted to a [ContentElement]
 */
@Serializable
data class NumericInputData(val data: Double) : InputData(){
    override val type = InputType.NUMERIC
    override fun toContent(): DescriptorContent<DoubleDescriptor> = TODO()
}

/**
 * [InputData] for a date.
 * Cannot be converted to a [ContentElement]
 */
@Serializable
data class DateInputData(val data: String) : InputData() {
    override val type = InputType.DATE
    override fun toContent(): ContentElement<*> = TODO()

    /**
     * Parses the input in YYYY-mm-dd format.
     */
    fun parseDate(): Date {
        val formatter = SimpleDateFormat("YYYY-mm-dd", Locale.ENGLISH)
        return formatter.parse(data)
    }
}
