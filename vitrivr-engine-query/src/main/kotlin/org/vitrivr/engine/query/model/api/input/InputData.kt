package org.vitrivr.engine.query.model.api.input

import io.github.oshai.kotlinlogging.KotlinLogging
import io.javalin.openapi.Discriminator
import io.javalin.openapi.DiscriminatorProperty
import io.javalin.openapi.OneOf
import kotlinx.serialization.Serializable
import org.vitrivr.engine.core.model.content.element.ContentElement
import org.vitrivr.engine.core.model.content.element.ImageContent
import org.vitrivr.engine.core.model.content.element.TextContent
import org.vitrivr.engine.core.model.content.impl.memory.InMemoryImageContent
import org.vitrivr.engine.core.model.content.impl.memory.InMemoryTextContent
import org.vitrivr.engine.core.util.extension.BufferedImage
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
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
        DateTimeInputData::class,
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
    override fun toContent(): ImageContent = InMemoryImageContent(BufferedImage(data))
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
 * [InputData] for a date, expected to be parsed as [LocalDateTime].
 * Cannot be converted to a [ContentElement].
 */
@Serializable
data class DateTimeInputData(val data: String, override val comparison: String? = "==") : InputData() {
    override val type = InputType.DATETIME

    override fun toContent(): ContentElement<*> {
        throw UnsupportedOperationException("Cannot derive content from DateTimeInputData")
    }

    companion object {
        private val logger = KotlinLogging.logger {}
        private val DATE_TIME_FORMATTERS: List<DateTimeFormatter> = listOf(

            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss[.SSSSSSSSS][.SSSSSS][.SSSSS][.SSSS][.SSS][.SS][.S]", Locale.ROOT),
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss[.SSSSSSSSS][.SSSSSS][.SSSSS][.SSSS][.SSS][.SS][.S]", Locale.ROOT),
            DateTimeFormatter.ofPattern("yyyy:MM:dd HH:mm:ss[.SSSSSSSSS][.SSSSSS][.SSSSS][.SSSS][.SSS][.SS][.S]", Locale.ROOT),

            DateTimeFormatter.ISO_LOCAL_DATE_TIME,
            DateTimeFormatter.ISO_LOCAL_DATE,

            DateTimeFormatter.ofPattern("yyyyMMdd_HHmm", Locale.ROOT) // LSC minute_id format in case user copies that
        )
    }

    /**
     * Parses the input 'data' string into a [LocalDateTime].
     * It tries a list of common date-time patterns.
     * If a date-only string (e.g., "2023-01-15") is parsed, it defaults to the start of that day (00:00:00).
     *
     * @return The parsed [LocalDateTime], or null if parsing fails with all known patterns.
     */
    fun toLocalDateTime(): LocalDateTime? {
        for (formatter in DATE_TIME_FORMATTERS) {
            try {
                // First, attempt to parse as LocalDateTime. This will work for most date-time strings
                return LocalDateTime.parse(this.data, formatter)
            } catch (e: DateTimeParseException) {
                logger.trace { "LocalDateTime parse failed for '${this.data}' with formatter '${formatter}' -> ${e.message}" }
                // If LocalDateTime parsing failed, specifically check if this formatter is ISO_LOCAL_DATE.
                if (formatter == DateTimeFormatter.ISO_LOCAL_DATE) {
                    try {
                        return LocalDate.parse(this.data, formatter).atStartOfDay()
                    } catch (e2: DateTimeParseException) {
                        logger.trace { "Fallback LocalDate parse also failed for '${this.data}' with ISO_LOCAL_DATE -> ${e2.message}" }
                    }
                }
            }
        }
        // If all formatters have been tried and none succeeded:
        logger.warn { "Failed to parse date string '${this.data}' into LocalDateTime using any predefined patterns." }
        return null
    }
    //TODO UNIT TESTS
}
