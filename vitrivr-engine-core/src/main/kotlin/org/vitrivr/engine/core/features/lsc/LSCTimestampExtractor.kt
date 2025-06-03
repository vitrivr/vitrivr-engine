package org.vitrivr.engine.core.features.lsc

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import com.drew.imaging.ImageMetadataReader
import com.drew.metadata.jpeg.JpegCommentDirectory
import org.vitrivr.engine.core.features.AbstractExtractor
import org.vitrivr.engine.core.model.content.ContentType
import org.vitrivr.engine.core.model.content.element.ImageContent
import org.vitrivr.engine.core.model.descriptor.Attribute
import org.vitrivr.engine.core.model.descriptor.struct.AnyMapStructDescriptor
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.model.retrievable.attributes.SourceAttribute
import org.vitrivr.engine.core.model.types.Type
import org.vitrivr.engine.core.model.types.Value
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.core.source.file.FileSource
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Pattern

/**
 * An [AbstractExtractor] implementation that extracts a timestamp derived from the 'minute_id'
 * found within a JSON string in the JPEG Comment (COM segment) of image files in the LSC Dataset.
 *
 * The extractor specifically targets the [JpegCommentDirectory] to find this JSON String
 * segment with information on the image.
 * It then parses the 'minute_id' (expected format "yyyyMMdd_HHmm") from the JSON content
 * into a [java.time.LocalDateTime] and stores it as a [Value.DateTime] in an [AnyMapStructDescriptor].
 *
 * If the JPEG comment, the 'minute_id' field, or a valid timestamp is not found, no descriptor is created.
 *
 * @author henrikluemkemann
 * @version 1.0.1
 */
class LSCTimestampExtractor : AbstractExtractor<ImageContent, AnyMapStructDescriptor> {

    /**
     * Companion object holding constants used by the [LSCTimestampExtractor].
     */
    companion object {
        /** Regex pattern to find and capture the 'minute_id' value from a JSON string. */
        private val MINUTE_ID_PATTERN = Pattern.compile("\"minute_id\":\"([0-9]{8}_[0-9]{4})\"")
        /** Date format pattern string for parsing the captured 'minute_id' value. */
        private const val MINUTE_ID_DATE_FORMAT_PATTERN = "yyyyMMdd_HHmm" // Renamed for clarity
        /** DateTimeFormatter for parsing the 'minute_id' value using Java Time API. */
        private val LSC_TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern(MINUTE_ID_DATE_FORMAT_PATTERN, Locale.ROOT)
        /** The attribute name used for storing the extracted timestamp in the resulting descriptor. */
        private const val ATTRIBUTE_NAME = "minuteIdTimestamp"
    }

    /**
     * Primary constructor for creating an [LSCTimestampExtractor] associated with a specific schema field.
     *
     * @param input The [Operator] that provides [Retrievable]s to this extractor.
     * @param analyser The [LSCTimestamp] analyser instance that configured this extractor.
     * @param field The [Schema.Field] definition for which this extractor produces descriptors.
     */
    constructor(
        input: Operator<Retrievable>,
        analyser: LSCTimestamp,
        field: Schema.Field<ImageContent, AnyMapStructDescriptor>
    ) : super(input, analyser, field)

    /**
     * Secondary constructor for creating an [LSCTimestampExtractor] identified by a name.
     *
     * @param input The [Operator] that provides [Retrievable]s to this extractor.
     * @param analyser The [LSCTimestamp] analyser instance that configured this extractor.
     * @param name The name identifying this extractor instance.
     */
    constructor(
        input: Operator<Retrievable>,
        analyser: LSCTimestamp,
        name: String
    ) : super(input, analyser, name)

    /**
     * The layout of the [AnyMapStructDescriptor] produced by this extractor.
     * It defines a single attribute named by [ATTRIBUTE_NAME] of type [Type.Datetime].
     */
    private val layout = listOf(Attribute(ATTRIBUTE_NAME, Type.Datetime))

    /**
     * Checks if a given [Retrievable] can be processed by this extractor.
     * This extractor matches [Retrievable]s that contain [ImageContent]
     * and have a [FileSource] attribute, as file access is needed to read the metadata.
     *
     * @param r The [Retrievable] to check.
     * @return `true` if the [Retrievable] contains a bitmap image and has a file source, `false` otherwise.
     */
    override fun matches(r: Retrievable): Boolean =
        r.content.any { it.type == ContentType.BITMAP_IMAGE } &&
                r.filteredAttribute(SourceAttribute::class.java)?.source is FileSource

    /**
     * Extracts 'minute_id' timestamp information from each [ImageContent] element of a [Retrievable].
     *
     * @param r The [Retrievable] to process.
     * @return A list containing a single [AnyMapStructDescriptor] if extraction succeeded, or an empty list otherwise.
     */
    override fun extract(r: Retrievable): List<AnyMapStructDescriptor> {
        logger.trace { "LSCTimestampExtractor: Starting extraction for retrievable ${r.id}" }

        val descriptor = descriptorFor(r) // This now returns AnyMapStructDescriptor?

        return if (descriptor != null) {
            // descriptorFor already sets retrievableId = r.id and field = this.field
            // in its success path, so no .copy() is needed here.
            listOf(descriptor)
        } else {
            emptyList() // No descriptor to return if extraction failed
        }
    }

    /**
     * Attempts to extract the 'minute_id' timestamp from the JPEG Comment of a [Retrievable]'s image file.
     *
     * The extractor:
     * - Reads the file via [FileSource]
     * - Looks for [JpegCommentDirectory] metadata
     * - Extracts the 'minute_id' string using a regex
     * - Parses the matched string into a [LocalDateTime].
     * - Returns an [AnyMapStructDescriptor] containing the timestamp
     *
     * If any step fails, a warning is logged and `null` is returned.
     *
     * @param r The [Retrievable] to process.
     * @return A populated [AnyMapStructDescriptor] or `null` on failure.
     */
    private fun descriptorFor(r: Retrievable): AnyMapStructDescriptor? {
        val src = r.filteredAttribute(SourceAttribute::class.java)?.source as? FileSource
            ?: run {
                logger.debug { "LSCTimestampExtractor: No FileSource for retrievable ${r.id}. Skipping descriptor creation." }
                return null
            }

        return runCatching {
            val metadata = ImageMetadataReader.readMetadata(src.path.toFile()) // TODO figure out how to read from stream
            val jpegCommentDir = metadata.getFirstDirectoryOfType(JpegCommentDirectory::class.java)

            var foundComment: String? = null

            if (jpegCommentDir != null) {
                logger.trace { "LSCTimestampExtractor: Checking Comment in JpegCommentDirectory for ${r.id}" }
                foundComment = jpegCommentDir.getString(JpegCommentDirectory.TAG_COMMENT)
                if (foundComment != null) {
                    logger.debug { "Found comment for ${r.id} in JpegCommentDirectory.Comment" }
                } else {
                    logger.trace { "LSCTimestampExtractor: No comment string found in JpegCommentDirectory for ${r.id}" }
                }
            } else {
                logger.trace { "LSCTimestampExtractor: No JpegCommentDirectory found for ${r.id}" }
            }

            if (foundComment == null) {
                logger.debug { "LSCTimestampExtractor: No comment found in JpegCommentDirectory for ${r.id}. Skipping descriptor creation." }
                return@runCatching null
            }


            logger.trace { "LSCTimestampExtractor: Using comment for ${r.id} from JpegCommentDirectory: '${foundComment.take(150)}...'" }

            val matcher = MINUTE_ID_PATTERN.matcher(foundComment)
            if (!matcher.find()) {
                logger.debug { "LSCTimestampExtractor: 'minute_id' pattern not found in comment from JpegCommentDirectory for ${r.id}. Comment snippet: ${foundComment.take(150)}. Skipping descriptor creation." }
                return@runCatching null
            }

            val minuteIdStr = matcher.group(1)
            if (minuteIdStr == null) {
                logger.warn { "LSCTimestampExtractor: 'minute_id' regex matched but group 1 is null (from JpegCommentDirectory) for ${r.id}. Skipping descriptor creation." }
                return@runCatching null
            }

            val parsedLocalDateTime: LocalDateTime = try {
                LocalDateTime.parse(minuteIdStr, LSC_TIMESTAMP_FORMATTER) // Use DateTimeFormatter
            } catch (e: DateTimeParseException) { // Catch DateTimeParseException
                logger.warn(e) { "LSCTimestampExtractor: Could not parse minute_id string '$minuteIdStr' (from JpegCommentDirectory) for ${r.id} into LocalDateTime. Skipping descriptor creation." }
                return@runCatching null
            }

            logger.info { "LSCTimestampExtractor: Extracted minute_id timestamp for ${r.id} as $parsedLocalDateTime (from '$minuteIdStr' in JpegCommentDirectory)" }

            AnyMapStructDescriptor(
                id = UUID.randomUUID(),
                retrievableId = r.id,
                layout = this.layout,
                values = mapOf(ATTRIBUTE_NAME to Value.DateTime(parsedLocalDateTime)),
                field = this.field
            )
        }.getOrElse { e ->
            logger.error(e) { "LSCTimestampExtractor: Unexpected error during extraction for ${r.id}. Skipping descriptor creation." }
            null
        }
    }
}