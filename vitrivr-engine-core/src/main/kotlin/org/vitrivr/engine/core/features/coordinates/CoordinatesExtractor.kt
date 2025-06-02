package org.vitrivr.engine.core.features.coordinates

import com.drew.imaging.ImageMetadataReader
import com.drew.metadata.jpeg.JpegCommentDirectory
import java.util.regex.Pattern
import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import org.vitrivr.engine.core.features.AbstractExtractor
import org.vitrivr.engine.core.features.metadata.source.file.FileSourceMetadataExtractor
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
import org.vitrivr.engine.core.operators.ingest.Extractor
import org.vitrivr.engine.core.source.file.FileSource
import java.util.*

val logger: KLogger = KotlinLogging.logger { }

/**
 * An [AbstractExtractor] implementation that extracts GPS coordinates from image metadata (EXIF-Files).
 *
 * @author henrikluemkemann
 * @version 1.0.1
 */
class CoordinatesExtractor : AbstractExtractor<ImageContent, AnyMapStructDescriptor> {

    constructor(input: Operator<Retrievable>, analyser: Coordinates, field: Schema.Field<ImageContent, AnyMapStructDescriptor>) : super(input, analyser, field)
    constructor(input: Operator<Retrievable>, analyser: Coordinates, name: String) : super(input, analyser, name)

    /** The layout of the [AnyMapStructDescriptor] produced by this [CoordinatesExtractor]. */
    private val layout = listOf(
        Attribute("lat", Type.Double),
        Attribute("lon", Type.Double)
    )

    /**
     * Internal method to check, if [Retrievable] matches this [Extractor] and should thus be processed.
     *
     * [FileSourceMetadataExtractor] implementation only works with [Retrievable]s that contain a [FileSource].
     *
     * @param retrievable The [Retrievable] to check.
     * @return True on match, false otherwise,
     */
    override fun matches(retrievable: Retrievable): Boolean = retrievable.content.any { it.type == ContentType.BITMAP_IMAGE }


    /**
     * Applies the extraction process to all [ImageContent] instances of a given [Retrievable].
     * Only successfully extracted descriptors are returned.
     *
     * @param retrievable The [Retrievable] from which the coordinates should be extracted.
     * @return A list of [AnyMapStructDescriptor]s representing extracted coordinates
     */
    override fun extract(retrievable: Retrievable): List<AnyMapStructDescriptor> {
        logger.trace { "CoordinatesExtractor: Starting extraction for retrievable ${retrievable.id}" }

        val imageContents = retrievable.content.filterIsInstance<ImageContent>()
        return imageContents.mapNotNull { imageContent -> extractCoordinates(imageContent, retrievable) }
    }



    /**
     * Extracts GPS coordinates from the image metadata.
     *
     * @param imageContent The image content to extract coordinates from
     * @param retrievable The retrievable containing the image content
     * @return An AnyMapStructDescriptor with the extracted coordinates
     */
    private fun extractCoordinates(
        imageContent: ImageContent,
        retrievable: Retrievable
    ): AnyMapStructDescriptor? {
        // first locate file on disk
        val source = retrievable.filteredAttribute(SourceAttribute::class.java)
            ?.source as? FileSource
            ?: run {
                logger.debug { "No FileSource for retrievable ${retrievable.id}. Skipping coordinate extraction." }
                return null
            }


        return try {

            val metadata = ImageMetadataReader.readMetadata(source.path.toFile())// TODO figure out how to read from stream
            val gpsDir = metadata.getFirstDirectoryOfType(
                com.drew.metadata.exif.GpsDirectory::class.java
            )

            var preciseLat: Double? = null
            var preciseLon: Double? = null
            var latRef: String? = null
            var lonRef: String? = null
            var sourceOfCoords = "EXIF GPS"

            // first, try to extract from rational arrays with high precision
            gpsDir?.let { dir ->
                // look for latitude tags
                val latTags = dir.tags.filter {
                    it.tagName.lowercase().contains("latitude") &&
                            !it.tagName.lowercase().contains("ref")
                }

                // look for longitude tags
                val lonTags = dir.tags.filter {
                    it.tagName.lowercase().contains("longitude") &&
                            !it.tagName.lowercase().contains("ref")
                }

                // Extract from rational arrays
                latTags.forEach { tag ->
                    runCatching {
                        val arr = dir.getRationalArray(tag.tagType) // this should return array of 3 rational values
                        if (arr.size == 3) {
                            //degrees + minutes/60 + seconds/3600
                            preciseLat = arr[0].toDouble() +
                                    arr[1].toDouble() / 60.0 +
                                    arr[2].toDouble() / 3600.0
                        }
                    }
                }

                lonTags.forEach { tag ->
                    runCatching {
                        val arr = dir.getRationalArray(tag.tagType)
                        if (arr.size == 3) {
                            preciseLon = arr[0].toDouble() +
                                    arr[1].toDouble() / 60.0 +
                                    arr[2].toDouble() / 3600.0
                        }
                    }
                }

                // Get reference tags [N, S, W, E]
                dir.tags.forEach { tag ->
                    val lower = tag.tagName.lowercase()
                    when {
                        lower.contains("latitude ref") -> latRef = tag.description.trim()
                        lower.contains("longitude ref") -> lonRef = tag.description.trim()//remove leading and trailing spaces
                    }
                }
            }

            // Apply reference adjustments
            var finalLat = preciseLat?.let {
                if (latRef == "S") -it else it
            }
            var finalLon = preciseLon?.let {
                if (lonRef == "W") -it else it
            }

            if (finalLat == null || finalLon == null) {
                logger.debug { "Primary EXIF GPS not found or incomplete for ${retrievable.id}. Attempting fallback to JPEG Comment." }
                // 'metadata' variable is already available from the EXIF GPS part
                try {
                    val jpegCommentDir = metadata.getFirstDirectoryOfType(JpegCommentDirectory::class.java)
                    if (jpegCommentDir != null) {
                        val jpegComment = jpegCommentDir.getString(JpegCommentDirectory.TAG_COMMENT)
                        // Basic check if comment exists and might contain relevant keys
                        if (jpegComment != null && jpegComment.contains("\"latitude\"") && jpegComment.contains("\"longitude\"")) {
                            logger.trace { "Found JPEG Comment for ${retrievable.id} (length: ${jpegComment.length}): '${jpegComment.take(150)}...'" }

                            val latFromComment = parseJsonNumber(jpegComment, "latitude")
                            val lonFromComment = parseJsonNumber(jpegComment, "longitude")

                            if (latFromComment != null && lonFromComment != null) {
                                // Use these values directly as finalLat and finalLon
                                finalLat = latFromComment
                                finalLon = lonFromComment
                                sourceOfCoords = "JPEG Comment" // Update the source
                                logger.info { "Successfully parsed lat/lon from JPEG Comment for ${retrievable.id}: lat=$finalLat, lon=$finalLon" }
                            } else {
                                logger.debug { "Latitude/Longitude keys found but values not parsable or missing in JPEG Comment JSON for ${retrievable.id}."}
                            }
                        } else {
                            logger.debug { "JPEG Comment string is null or does not appear to contain coordinate keywords for ${retrievable.id}." }
                        }
                    } else {
                        logger.debug { "No JpegCommentDirectory found for ${retrievable.id}." }
                    }
                } catch (e: Exception) {
                    logger.warn(e) { "Error during JPEG Comment fallback for GPS coordinates on ${retrievable.id}: ${e.message}" }
                }
            }

            // Create the descriptor
            if (finalLat != null && finalLon != null) {
                logger.info { "Extracted coordinates (Source: $sourceOfCoords): lat=$finalLat, lon=$finalLon" }

                return AnyMapStructDescriptor(
                    UUID.randomUUID(),
                    retrievable.id,
                    layout,
                    mapOf(
                        "lat" to Value.Double(finalLat),
                        "lon" to Value.Double(finalLon)
                    ),
                    this.field
                )
            }

            // If no coordinates were found:
            logger.debug { "No valid GPS coordinates found (after EXIF and JPEG Comment fallback) for retrievable ${retrievable.id}. Skipping descriptor creation." }
            null
        } catch (e: Exception) {
            logger.error(e) { "Error extracting GPS for retrievable ${retrievable.id}. Skipping descriptor creation." }
            null
        }
    }

    /**
     * Helper function to parse a numeric value for a given key from a JSON string using regex.
     *
     * @param jsonString The JSON string to parse.
     * @param key The key whose numeric value is to be extracted.
     * @return The parsed [Double] value, or null if the key is not found or value is not a valid number.
     */
    private fun parseJsonNumber(jsonString: String, key: String): Double? {
        val pattern = Pattern.compile("\"$key\":\\s*(-?[0-9]+\\.?[0-9]*(?:[eE][-+]?[0-9]+)?)")
        val matcher = pattern.matcher(jsonString)
        return if (matcher.find()) {
            matcher.group(1)?.toDoubleOrNull().also {
                if (it == null && matcher.group(1) != null) {
                    // Log if group was found but couldn't be parsed as Double
                    logger.trace { "Failed to parse value for key '$key': '${matcher.group(1)}' as Double from JSON." }
                }
            }
        } else {
            logger.trace { "Key '$key' not found in JSON string for parsing." }
            null
        }
    }
}