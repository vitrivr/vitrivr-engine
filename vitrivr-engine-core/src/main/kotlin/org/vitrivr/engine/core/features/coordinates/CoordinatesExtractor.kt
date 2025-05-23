package org.vitrivr.engine.core.features.coordinates

import com.drew.imaging.ImageMetadataReader
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
     * [FileSourceMetadataExtractor] implementation only works with [Retrievable] that contain a [FileSource].
     *
     * @param retrievable The [Retrievable] to check.
     * @return True on match, false otherwise,
     */
    override fun matches(retrievable: Retrievable): Boolean = retrievable.content.any { it.type == ContentType.BITMAP_IMAGE }


    override fun extract(retrievable: Retrievable): List<AnyMapStructDescriptor> {
        logger.trace { "CoordinatesExtractor: Starting extraction for retrievable ${retrievable.id}" }

        // Get all ImageContent elements from the retrievable
        val imageContents = retrievable.content.filterIsInstance<ImageContent>()

        // Process each ImageContent element and create a descriptor for eahc one
        return imageContents.map { imageContent ->
            val descriptor = extractCoordinates(imageContent, retrievable)

            // Set the retrievable ID and field in the descriptor
            descriptor.copy(retrievableId = retrievable.id, field = this.field)
        }
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
    ): AnyMapStructDescriptor {
        // first locate file on disk
        val source = retrievable.filteredAttribute(SourceAttribute::class.java)
            ?.source as? FileSource
            ?: return createEmptyDescriptor(retrievable.id).also {
                logger.debug { "No FileSource for retrievable ${retrievable.id}" }
            }

        return try {

            val metadata = ImageMetadataReader.readMetadata(source.path.toFile())
            val gpsDir = metadata.getFirstDirectoryOfType(
                com.drew.metadata.exif.GpsDirectory::class.java
            )

            var preciseLat: Double? = null
            var preciseLon: Double? = null
            var latRef: String? = null
            var lonRef: String? = null

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
                            // Preserve full precision: degrees + minutes/60 + seconds/3600
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

            // If rational array method doesnt work, try parsing the tags
            if (preciseLat == null) {
                gpsDir?.tags?.forEach { tag ->
                    if (tag.tagName.lowercase().contains("latitude") &&
                        !tag.tagName.lowercase().contains("ref")) {
                        preciseLat = parseCoordinate(tag.description)
                    }
                }
            }

            if (preciseLon == null) {
                gpsDir?.tags?.forEach { tag ->
                    if (tag.tagName.lowercase().contains("longitude") &&
                        !tag.tagName.lowercase().contains("ref")) {
                        preciseLon = parseCoordinate(tag.description)
                    }
                }
            }

            // Apply reference adjustments
            val finalLat = preciseLat?.let {
                if (latRef == "S") -it else it
            }
            val finalLon = preciseLon?.let {
                if (lonRef == "W") -it else it
            }

            // Create the descriptor
            if (finalLat != null && finalLon != null) {
                logger.info { "Extracted high-precision coordinates: lat=$finalLat, lon=$finalLon" }

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

            // fallback search if no coordinates were found (unlikely for EXIF files)
            metadata.directories.forEach { dir ->
                dir.tags.forEach { tag ->
                    val name = tag.tagName.lowercase()
                    when {
                        name.contains("latitude") && !name.contains("ref") -> {
                            val parsed = parseCoordinate(tag.description)
                            logger.debug { "Found alternate latitude coordinate: $parsed" }

                            return AnyMapStructDescriptor(
                                UUID.randomUUID(),
                                retrievable.id,
                                layout,
                                mapOf(
                                    "lat" to Value.Double(parsed),
                                    "lon" to Value.Double(parsed)
                                ),
                                this.field
                            )
                        }
                    }
                }
            }

            // If no coordinates were found:
            logger.debug { "No precise GPS coordinates found for retrievable ${retrievable.id}" }
            createEmptyDescriptor(retrievable.id)
        } catch (e: Exception) {
            logger.error(e) { "Error extracting GPS for retrievable ${retrievable.id}" }
            createEmptyDescriptor(retrievable.id)
        }
    }

    /**
     * Creates an empty descriptor with null values for latitude and longitude.
     * This is used when no GPS coordinates are found in the image metadata.
     *
     * @param retrievableId The ID of the retrievable
     * @return An AnyMapStructDescriptor with null values for lat and lon
     */
    private fun createEmptyDescriptor(retrievableId: UUID): AnyMapStructDescriptor {
        return AnyMapStructDescriptor(
            UUID.randomUUID(),
            retrievableId,
            layout,
            mapOf(
                "lat" to null,
                "lon" to null
            ),
            this.field
        )
    }

    /**
     * Parses a coordinate string in different formats and converts it to decimal degrees.
     * This method is used as a fallback when direct access to preferred format values is not possible.
     *
     * Handles formats like:
     * - "52° 13' 26.43\"" (DMS format)
     * - "52° 13.456'" (DM format)
     * - "52.123456°" (decimal degrees)
     * - "52.123456" (plain decimal)
     * - "52 deg 13 min 26.43 sec" (text format)
     * - "52 13 26.43" (space-separated DMS)
     */
    private fun parseCoordinate(coordStr: String): Double {
        logger.debug { "Parsing coordinate string: $coordStr" }

        // Try to handle DMS format with direction: "47º 8' 37.51\" N" or similar
        val dmsPatternWithDirection = Regex("""(\d+)º\s*(\d+)['′]?\s*(\d+(?:\.\d+)?)["″]?\s*([NSEW])""")
        val dmsMatchWithDirection = dmsPatternWithDirection.find(coordStr)
        if (dmsMatchWithDirection != null) {
            val (degrees, minutes, seconds, direction) = dmsMatchWithDirection.destructured
            var result = degrees.toDouble() + minutes.toDouble() / 60.0 + seconds.toDouble() / 3600.0
            if (direction == "S" || direction == "W") {
                result = -result
            }
            logger.debug { "Parsed DMS format with direction: $degrees° $minutes' $seconds\" $direction = $result" }
            return result
        }

        // Try to handle DMS format with º symbol: "47º 8' 37.51\"" or similar
        val dmsPatternWithDegreeSymbol = Regex("""(\d+)º\s*(\d+)['′]?\s*(\d+(?:\.\d+)?)["″]?""")
        val dmsMatchWithDegreeSymbol = dmsPatternWithDegreeSymbol.find(coordStr)
        if (dmsMatchWithDegreeSymbol != null) {
            val (degrees, minutes, seconds) = dmsMatchWithDegreeSymbol.destructured
            val result = degrees.toDouble() + minutes.toDouble() / 60.0 + seconds.toDouble() / 3600.0
            logger.debug { "Parsed DMS format with º symbol: $degrees° $minutes' $seconds\" = $result" }
            return result
        }

        // Try to handle DMS format: "52° 13' 26.43\"" or similar
        val dmsPattern = Regex("""(\d+(?:\.\d+)?)°\s*(\d+(?:\.\d+)?)['′]?\s*(\d+(?:\.\d+)?)["″]?""")
        val dmsMatch = dmsPattern.find(coordStr)
        if (dmsMatch != null) {
            val (degrees, minutes, seconds) = dmsMatch.destructured
            val result = degrees.toDouble() + minutes.toDouble() / 60.0 + seconds.toDouble() / 3600.0
            logger.debug { "Parsed DMS format: $degrees° $minutes' $seconds\" = $result" }
            return result
        }

        // Try to handle DM format: "52° 13.456'" or similar
        val dmPattern = Regex("""(\d+(?:\.\d+)?)°\s*(\d+(?:\.\d+)?)['′]""")
        val dmMatch = dmPattern.find(coordStr)
        if (dmMatch != null) {
            val (degrees, minutes) = dmMatch.destructured
            val result = degrees.toDouble() + minutes.toDouble() / 60.0
            logger.debug { "Parsed DM format: $degrees° $minutes' = $result" }
            return result
        }

        // Try to handle decimal degrees with symbol: "52.123456°"
        val decimalDegPattern = Regex("""(\d+(?:\.\d+)?)°""")
        val decimalDegMatch = decimalDegPattern.find(coordStr)
        if (decimalDegMatch != null) {
            val result = decimalDegMatch.groupValues[1].toDouble()
            logger.debug { "Parsed decimal degrees with symbol: $result°" }
            return result
        }

        // Try to handle text format: "52 deg 13 min 26.43 sec"
        val textPattern = Regex("""(\d+(?:\.\d+)?)\s*deg(?:rees?)?\s*(\d+(?:\.\d+)?)\s*min(?:utes?)?\s*(\d+(?:\.\d+)?)\s*sec(?:onds?)?""", RegexOption.IGNORE_CASE)
        val textMatch = textPattern.find(coordStr)
        if (textMatch != null) {
            val (degrees, minutes, seconds) = textMatch.destructured
            val result = degrees.toDouble() + minutes.toDouble() / 60.0 + seconds.toDouble() / 3600.0
            logger.debug { "Parsed text format: $degrees deg $minutes min $seconds sec = $result" }
            return result
        }

        // Try to handle space-separated DMS: "52 13 26.43"
        val spaceDmsPattern = Regex("""^(\d+(?:\.\d+)?)\s+(\d+(?:\.\d+)?)\s+(\d+(?:\.\d+)?)$""")
        val spaceDmsMatch = spaceDmsPattern.find(coordStr)
        if (spaceDmsMatch != null) {
            val (degrees, minutes, seconds) = spaceDmsMatch.destructured
            val result = degrees.toDouble() + minutes.toDouble() / 60.0 + seconds.toDouble() / 3600.0
            logger.debug { "Parsed space-separated DMS: $degrees $minutes $seconds = $result" }
            return result
        }

        // Try to handle comma-separated rational values: "47/1, 8/1, 3751/100"
        val rationalPattern = Regex("""(\d+)/(\d+),\s*(\d+)/(\d+),\s*(\d+)/(\d+)""")
        val rationalMatch = rationalPattern.find(coordStr)
        if (rationalMatch != null) {
            val (degNum, degDenom, minNum, minDenom, secNum, secDenom) = rationalMatch.destructured
            val degrees = degNum.toDouble() / degDenom.toDouble()
            val minutes = minNum.toDouble() / minDenom.toDouble()
            val seconds = secNum.toDouble() / secDenom.toDouble()
            val result = degrees + minutes / 60.0 + seconds / 3600.0
            logger.debug { "Parsed rational format: $degrees° $minutes' $seconds\" = $result" }
            return result
        }

        // Last resort: try to find any decimal number in the string
        val decimalPattern = Regex("""(\d+(?:\.\d+)?)""")
        val decimalMatch = decimalPattern.find(coordStr)
        if (decimalMatch != null) {
            val result = decimalMatch.groupValues[1].toDouble()
            logger.debug { "Parsed decimal number: $result" }
            return result
        }

        logger.warn { "Could not parse coordinate: $coordStr" }
        throw IllegalArgumentException("Could not parse coordinate: $coordStr")
    }
}