package org.vitrivr.engine.query.model.api.input

import io.javalin.openapi.Discriminator
import io.javalin.openapi.DiscriminatorProperty
import io.javalin.openapi.OneOf
import io.javalin.openapi.OpenApiIgnore
import kotlinx.serialization.Serializable
import org.vitrivr.engine.core.model.content.element.ContentElement
import org.vitrivr.engine.core.model.content.element.ImageContent
import org.vitrivr.engine.core.model.content.element.TextContent
import org.vitrivr.engine.core.model.content.impl.memory.InMemoryImageContent
import org.vitrivr.engine.core.model.content.impl.memory.InMemoryTextContent
import org.vitrivr.engine.core.util.extension.BufferedImage
import java.text.SimpleDateFormat
import java.util.*

/**
 * The abstract [InputData], essentially a query's input value.
 */
@Serializable(with = InputDataSerializer::class)
@OneOf(
    discriminator = Discriminator(DiscriminatorProperty("type", type = String::class)),
    value = [
        TextInputData::class,
        ImageInputData::class,
        FloatVectorInputData::class,
        RetrievableIdInputData::class,
        BooleanInputData::class,
        NumericInputData::class,
        DateInputData::class
    ]
)
abstract class InputData() {
    /**
     * The type name of this [InputData]. Required for polymorphic deserialisation.
     */
    abstract val type: String

}

abstract class ContentInputData : InputData() {

    @OpenApiIgnore
    abstract fun toContent() : ContentElement<*>

}

/**
 * [InputData] for textual input.
 * Can be converted to a [ContentElement], specifically a [TextContent].
 */
@Serializable
data class TextInputData(val data: String) : ContentInputData() {
    override val type = "TEXT"
    @OpenApiIgnore
    override fun toContent(): TextContent = InMemoryTextContent(data)
}

/**
 * [InputData] for vector input.
 * Cannot be converted to a [ContentElement]
 */
@Serializable
data class FloatVectorInputData(val data: List<Float>) : InputData(){
    override val type = "FLOAT_VECTOR"
}

/**
 * [InputData] for image input in base64 format.
 * Can be converted to a [ContentElement], specifically to a [InMemoryImageContent].
 */
@Serializable
data class ImageInputData(val data: String) : ContentInputData() {
    override val type = "IMAGE"
    @OpenApiIgnore
    override fun toContent(): ImageContent = InMemoryImageContent(BufferedImage(data))
}

/**
 * [InputData] for a retrievable id.
 * Cannot be converted to a [ContentElement]
 */
@Serializable
data class RetrievableIdInputData(val id: String) : InputData() {
    override val type = "ID"
}

/**
 * [InputData] for boolean input.
 * Cannot be converted to a [ContentElement]
 */
@Serializable
data class BooleanInputData(val data: Boolean): InputData(){
    override val type = "BOOLEAN"
}

/**
 * [InputData] for numeric input.
 * Cannot be converted to a [ContentElement]
 */
@Serializable
data class NumericInputData(val data: Double) : InputData(){
    override val type = "NUMERIC"
}

/**
 * [InputData] for a date.
 * Cannot be converted to a [ContentElement]
 */
@Serializable
data class DateInputData(val data: String) : InputData() {
    override val type = "DATE"

    /**
     * Parses the input in YYYY-mm-dd format.
     */
    @OpenApiIgnore
    fun parseDate(): Date {
        val formatter = SimpleDateFormat("YYYY-mm-dd", Locale.ENGLISH)
        return formatter.parse(data)
    }
}

@Serializable
data class ListInputData(val data: List<InputData>) : InputData() {
    override val type = "LIST"
}

@Serializable
data class StructInputData(val data: Map<String, InputData>) : InputData() {
    override val type = "STRUCT"
}