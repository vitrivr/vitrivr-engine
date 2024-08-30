package org.vitrivr.engine.query.model.api.input

import kotlinx.serialization.Serializable
import org.vitrivr.engine.core.model.content.element.ContentElement
import org.vitrivr.engine.core.model.content.element.ImageContent
import org.vitrivr.engine.core.model.content.element.TextContent
import org.vitrivr.engine.core.model.content.impl.memory.InMemoryImageContent
import org.vitrivr.engine.core.model.content.impl.memory.InMemoryTextContent
import org.vitrivr.engine.core.util.extension.BufferedImage
import java.awt.image.BufferedImage
import java.text.SimpleDateFormat
import java.util.*

/**
 * The abstract [InputData], essentially a query's input value.
 */
@Serializable(with = InputDataSerializer::class)
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

    /**
     * Optional comparison to apply.
     *
     * Currently supported comparisons use Kotlin notation:
     * - `<`: less than
     * - `<=`: less or equal than
     * - `==`: equal
     * - `!=`: not equal
     * - `>=`: greater or equal than
     * - `>` : greater than
     * - `~=`: LIKE
     */
    abstract val comparison: String?
}

/**
 * [InputData] for textual input.
 * Can be converted to a [ContentElement], specifically a [TextContent].
 */
@Serializable
data class TextInputData(val data: String, override val comparison: String? = "==") : InputData() {
    override val type = InputType.TEXT

    override fun toContent(): TextContent = InMemoryTextContent(data)
}

/**
 * [InputData] for vector input.
 * Cannot be converted to a [ContentElement]
 */
@Serializable
data class VectorInputData(val data: List<Float>, override val comparison: String? = "==") : InputData(){
    override val type = InputType.VECTOR

    override fun toContent(): ContentElement<*> {
        throw UnsupportedOperationException("Cannot derive content from VectorInputData")
    }

}

/**
 * [InputData] for image input in base64 format.
 * Can be converted to a [ContentElement], specifically to a [InMemoryImageContent].
 */
@Serializable
data class ImageInputData(val data: String, override val comparison: String? = "==") : InputData() {
    override val type = InputType.VECTOR
    override fun toContent(): ImageContent = InMemoryImageContent(image)

    /**
     * [BufferedImage] representation of the base64 input.
     */
    private val image: BufferedImage by lazy { BufferedImage(data) }

}

/**
 * [InputData] for a retrievable id.
 * Cannot be converted to a [ContentElement]
 */
@Serializable
data class RetrievableIdInputData(val id: String, override val comparison: String? = "==") : InputData() {

    override val type = InputType.ID

    override fun toContent(): ContentElement<*> {
        throw UnsupportedOperationException("Cannot derive content from RetrievableInputData")
    }

}

/**
 * [InputData] for boolean input.
 * Cannot be converted to a [ContentElement]
 */
@Serializable
data class BooleanInputData(val data: Boolean, override val comparison: String? = "=="): InputData(){
    override val type = InputType.BOOLEAN
    override fun toContent(): ContentElement<*> {
        throw UnsupportedOperationException("Cannot derive content from BooleanInputData")
    }
}

/**
 * [InputData] for numeric input.
 * Cannot be converted to a [ContentElement]
 */
@Serializable
data class NumericInputData(val data: Double, override val comparison: String? = "==") : InputData(){
    override val type = InputType.NUMERIC
    override fun toContent(): ContentElement<*> {
        throw UnsupportedOperationException("Cannot derive content from NumericInputData")
    }
}

/**
 * [InputData] for a date.
 * Cannot be converted to a [ContentElement]
 */
@Serializable
data class DateInputData(val data: String, override val comparison: String? = "==") : InputData() {
    override val type = InputType.DATE
    override fun toContent(): ContentElement<*> {throw UnsupportedOperationException("Cannot derive content from DateInputData")}

    /**
     * Parses the input in YYYY-mm-dd format.
     */
    fun parseDate():Date{
        val formatter = SimpleDateFormat("YYYY-mm-dd", Locale.ENGLISH)
        return formatter.parse(data)
    }
}
