package org.vitrivr.engine.module.features.feature.lsc.coordinates

import com.drew.imaging.ImageMetadataReader
import com.drew.metadata.exif.GpsDirectory
import com.drew.metadata.jpeg.JpegCommentDirectory
import java.util.regex.Pattern
import org.vitrivr.engine.core.features.AbstractExtractor
import org.vitrivr.engine.core.model.content.ContentType
import org.vitrivr.engine.core.model.content.element.ImageContent
import org.vitrivr.engine.core.model.descriptor.Attribute
import org.vitrivr.engine.core.model.descriptor.DescriptorId
import org.vitrivr.engine.core.model.descriptor.struct.AnyMapStructDescriptor
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.model.retrievable.RetrievableId
import org.vitrivr.engine.core.model.retrievable.attributes.SourceAttribute
import org.vitrivr.engine.core.model.types.Type
import org.vitrivr.engine.core.model.types.Value
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.core.source.file.FileSource
import java.util.UUID
import kotlin.collections.get
import kotlin.text.contains
import kotlin.text.take

/**
 * Implementation of an [AbstractExtractor] that extracts GPS coordinates from the EXIF metadata of an image file.
 *
 * The extracted latitude and longitude values are transformed into a WKT (Well-Known Text) 'POINT' string, following the
 * PostGIS standard format ('POINT(longitude latitude)'), and stored as a [Value.GeographyValue].
 *
 * If valid coordinates cannot be extracted, no descriptor is created/persisted.
 *
 * @author henrikluemkemann
 * @version 1.2.1
 */
class PostGISCoordinatesExtractor :
    AbstractExtractor<ImageContent, AnyMapStructDescriptor> {

    constructor(
        input: Operator<Retrievable>,
        analyser: PostGISCoordinates,
        field: Schema.Field<ImageContent, AnyMapStructDescriptor>
    ) : super(input, analyser, field)

    constructor(
        input: Operator<Retrievable>,
        analyser: PostGISCoordinates,
        name: String
    ) : super(input, analyser, name)

    /**
     * The name of the attribute that will store the geography data.
     * This uses the configured name of the extractor instance.
     */
    private val geographyAttributeName: String get() = this.name


    /**
     * Checks whether the given [Retrievable] contains at least one [ImageContent] element.
     *
     * @param retrievable The retrievable to test for compatibility.
     * @return `true` if at least one image is present, `false` otherwise.
     */
    override fun matches(retrievable: Retrievable): Boolean = retrievable.content.any { it.type == ContentType.BITMAP_IMAGE }


    /**
     * Applies the extraction process to all [ImageContent] instances of a given [Retrievable].
     *
     * @param retrievable The [Retrievable] from which GPS data should be extracted.
     * @return A list of [AnyMapStructDescriptor]s representing extracted GPS points.
     */
    override fun extract(retrievable: Retrievable): List<AnyMapStructDescriptor> {
        logger.trace { "PostGISCoordinatesExtractor: Starting extraction for retrievable ${retrievable.id}" }

        val imageContents = retrievable.content.filterIsInstance<ImageContent>()

        return imageContents.mapNotNull { imageContent ->
            extractGeographyData(imageContent, retrievable)
        }
    }

    /**
     * Extracts and converts GPS metadata (latitude and longitude) into a PostGIS-compatible WKT point.
     *
     * @param imageContent The [ImageContent] to extract metadata from.
     * @param retrievable The parent [Retrievable] object, used to access an original file via [FileSource].
     * @return A populated [AnyMapStructDescriptor] or null if no coordinates are found.
     */
    private fun extractGeographyData(
        imageContent: ImageContent,
        retrievable: Retrievable
    ): AnyMapStructDescriptor? {
        val source = retrievable.filteredAttribute(SourceAttribute::class.java)
            ?.source as? FileSource
            ?: run {
                logger.debug { "No FileSource for retrievable ${retrievable.id}. Skipping PostGIS coordinate extraction." }
                return null
            }

        return try {
            val metadata = ImageMetadataReader.readMetadata(source.path.toFile()) // TODO figure out how to read from stream
            val gpsDir = metadata.getFirstDirectoryOfType(GpsDirectory::class.java)

            var preciseLat: Double? = null
            var preciseLon: Double? = null
            var latRef: String? = null
            var lonRef: String? = null
            var sourceOfCoords = "EXIF GPS"

            gpsDir?.let { dir ->
                dir.tags.filter { it.tagName.lowercase().contains("latitude") && !it.tagName.lowercase().contains("ref") }
                    .forEach { tag ->
                        runCatching {
                            val arr = dir.getRationalArray(tag.tagType)
                            if (arr.size == 3) {
                                preciseLat = arr[0].toDouble() + arr[1].toDouble() / 60.0 + arr[2].toDouble() / 3600.0
                            }
                        }.onFailure { e -> logger.trace(e) { "Failed to parse rational array for latitude tag: ${tag.tagName}" } }
                    }

                dir.tags.filter { it.tagName.lowercase().contains("longitude") && !it.tagName.lowercase().contains("ref") }
                    .forEach { tag ->
                        runCatching {
                            val arr = dir.getRationalArray(tag.tagType)
                            if (arr.size == 3) {
                                preciseLon = arr[0].toDouble() + arr[1].toDouble() / 60.0 + arr[2].toDouble() / 3600.0
                            }
                        }.onFailure { e -> logger.trace(e) { "Failed to parse rational array for longitude tag: ${tag.tagName}" } }
                    }

                dir.tags.forEach { tag ->
                    val lower = tag.tagName.lowercase()
                    when {
                        lower.contains("latitude ref") -> latRef = tag.description?.trim()
                        lower.contains("longitude ref") -> lonRef = tag.description?.trim()
                    }
                }
            }

            var finalLat = preciseLat?.let { if (latRef == "S") -it else it }
            var finalLon = preciseLon?.let { if (lonRef == "W") -it else it }

            // fallback (and LSC) logic, that  looks through JPEG Comments for coordinates.
            if (finalLat == null || finalLon == null) {
                logger.debug { "Primary EXIF GPS not found or incomplete for ${retrievable.id}. Attempting fallback to JPEG Comment." }
                try {
                    val jpegCommentDir = metadata.getFirstDirectoryOfType(JpegCommentDirectory::class.java)
                    if (jpegCommentDir != null) {
                        val jpegComment = jpegCommentDir.getString(JpegCommentDirectory.TAG_COMMENT)

                        if (jpegComment != null && jpegComment.contains("\"latitude\"") && jpegComment.contains("\"longitude\"")) {
                            logger.trace { "Found JPEG Comment for ${retrievable.id} (length: ${jpegComment.length}): '${jpegComment.take(150)}...'" }

                            val latFromComment = parseJsonNumber(jpegComment, "latitude")
                            val lonFromComment = parseJsonNumber(jpegComment, "longitude")

                            if (latFromComment != null && lonFromComment != null) {
                                // Overwrite/set finalLat and finalLon if we successfully parsed from JPEG comment
                                finalLat = latFromComment
                                finalLon = lonFromComment
                                sourceOfCoords = "JPEG Comment"
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
                    // If an error occurs in fallback, finalLat/finalLon will remain as they were (likely null)
                }
            }

            if (finalLat != null && finalLon != null) {
                logger.info { "Extracted coordinates for ${retrievable.id} (Source: $sourceOfCoords): lat=$finalLat, lon=$finalLon" }
                val wktPoint = "POINT($finalLon $finalLat)" // Standard WKT: Longitude first, then Latitude
                val geographyValue = Value.GeographyValue(wktPoint)

                return AnyMapStructDescriptor(
                    id = UUID.randomUUID() as DescriptorId,
                    retrievableId = retrievable.id as RetrievableId?,
                    layout = listOf(Attribute(geographyAttributeName, Type.Geography)),
                    values = mapOf(geographyAttributeName to geographyValue),
                    field = this.field
                )
            }

            logger.debug { "No valid GPS coordinates found (after EXIF and JPEG Comment fallback) for retrievable ${retrievable.id}. Skipping..." }
            null
        } catch (e: Exception) {
            logger.error(e) { "Error extracting GPS for retrievable ${retrievable.id}. Skipping..." }
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