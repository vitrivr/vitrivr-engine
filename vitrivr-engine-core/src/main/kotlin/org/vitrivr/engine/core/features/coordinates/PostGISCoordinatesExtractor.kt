package org.vitrivr.engine.core.features.coordinates

import com.drew.imaging.ImageMetadataReader
import com.drew.metadata.exif.GpsDirectory
import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging // Ensure this import is present
import org.vitrivr.engine.core.features.AbstractExtractor
import org.vitrivr.engine.core.model.content.ContentType
import org.vitrivr.engine.core.model.content.element.ImageContent
import org.vitrivr.engine.core.model.descriptor.Attribute
import org.vitrivr.engine.core.model.descriptor.DescriptorId // Assuming typealias DescriptorId = UUID
import org.vitrivr.engine.core.model.descriptor.struct.AnyMapStructDescriptor
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.model.retrievable.RetrievableId // Assuming typealias RetrievableId = UUID
import org.vitrivr.engine.core.model.retrievable.attributes.SourceAttribute
import org.vitrivr.engine.core.model.types.Type
import org.vitrivr.engine.core.model.types.Value
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.core.source.file.FileSource
import java.util.UUID

/**
 * An [AbstractExtractor] implementation that extracts GPS coordinates from image metadata (EXIF)
 * and stores them as a single PostGIS-compatible geography value (POINT WKT).
 *
 * The name of the output attribute will be the configured name of this extractor instance.
 *
 * @author henrikluemkemann
 * @version 1.2.1
 */
class PostGISCoordinatesExtractor :
    AbstractExtractor<ImageContent, AnyMapStructDescriptor> {

    // Private logger for this class
    private companion object {
        val logger: KLogger = KotlinLogging.logger {}
    }

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
    private val geographyAttributeName: String
        get() = this.name // 'this.name' is from AbstractExtractor

    override fun matches(retrievable: Retrievable): Boolean =
        retrievable.content.any { it.type == ContentType.BITMAP_IMAGE }

    override fun extract(retrievable: Retrievable): List<AnyMapStructDescriptor> {
        logger.trace { "PostGISCoordinatesExtractor: Starting extraction for retrievable ${retrievable.id}" }

        val imageContents = retrievable.content.filterIsInstance<ImageContent>()

        return imageContents.mapNotNull { imageContent ->
            extractGeographyData(imageContent, retrievable)
        }
    }

    /**
     * Extracts GPS coordinates and transforms them into a single geography attribute.
     */
    private fun extractGeographyData(
        imageContent: ImageContent,
        retrievable: Retrievable
    ): AnyMapStructDescriptor? {
        val source = retrievable.filteredAttribute(SourceAttribute::class.java)
            ?.source as? FileSource
            ?: return createEmptyGeographyDescriptor(retrievable.id).also { // Corrected: Pass retrievable.id
                logger.debug { "No FileSource for retrievable ${retrievable.id}" }
            }

        return try {
            val metadata = ImageMetadataReader.readMetadata(source.path.toFile())
            val gpsDir = metadata.getFirstDirectoryOfType(GpsDirectory::class.java)

            var preciseLat: Double? = null
            var preciseLon: Double? = null
            var latRef: String? = null
            var lonRef: String? = null

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

            if (preciseLat == null) {
                gpsDir?.tags?.firstOrNull { it.tagName.lowercase().contains("latitude") && !it.tagName.lowercase().contains("ref") }
                    ?.let { preciseLat = parseCoordinate(it.description) }
            }

            if (preciseLon == null) {
                gpsDir?.tags?.firstOrNull { it.tagName.lowercase().contains("longitude") && !it.tagName.lowercase().contains("ref") }
                    ?.let { preciseLon = parseCoordinate(it.description) }
            }

            val finalLat = preciseLat?.let { if (latRef == "S") -it else it }
            val finalLon = preciseLon?.let { if (lonRef == "W") -it else it }

            if (finalLat != null && finalLon != null) {
                logger.info { "Extracted coordinates for ${retrievable.id}: lat=$finalLat, lon=$finalLon" }
                val wktPoint = "POINT($finalLon $finalLat)" // Standard WKT: Longitude first, then Latitude
                val geographyValue = Value.GeographyValue(wktPoint) // SRID 4326 is default

                return AnyMapStructDescriptor(
                    id = UUID.randomUUID() as DescriptorId,
                    retrievableId = retrievable.id as RetrievableId?,
                    layout = listOf(Attribute(geographyAttributeName, Type.Geography)),
                    values = mapOf(geographyAttributeName to geographyValue),
                    field = this.field
                )
            }

            logger.debug { "No valid GPS coordinates found for retrievable ${retrievable.id}" }
            createEmptyGeographyDescriptor(retrievable.id) // Corrected: Pass retrievable.id
        } catch (e: Exception) {
            logger.error(e) { "Error extracting GPS for retrievable ${retrievable.id}" }
            createEmptyGeographyDescriptor(retrievable.id) // Corrected: Pass retrievable.id
        }
    }

    /**
     * Creates a descriptor with a default geography value.
     */
    private fun createEmptyGeographyDescriptor(retrievableIdToUse: RetrievableId): AnyMapStructDescriptor { // Changed parameter name for clarity
        logger.debug { "Creating empty/default geography descriptor for $retrievableIdToUse" }
        val defaultGeoValue = Type.Geography.defaultValue()
        return AnyMapStructDescriptor(
            id = UUID.randomUUID() as DescriptorId,
            retrievableId = retrievableIdToUse as RetrievableId?, // Used corrected parameter
            layout = listOf(Attribute(geographyAttributeName, Type.Geography)),
            values = mapOf(geographyAttributeName to defaultGeoValue),
            field = this.field
        )
    }

    /**
     * Parses a coordinate string in different formats and converts it to decimal degrees.
     */
    private fun parseCoordinate(coordStr: String?): Double? {
        if (coordStr == null) return null
        logger.debug { "Parsing coordinate string: $coordStr" }

        // DMS format with direction: "47º 8' 37.51\" N" or similar
        val dmsPatternWithDirection = Regex("""(\d+)º\s*(\d+)['′]?\s*(\d+(?:\.\d+)?)["″]?\s*([NSEWnsew])""")
        dmsPatternWithDirection.find(coordStr)?.let {
            val (degrees, minutes, seconds, direction) = it.destructured
            var result = degrees.toDouble() + minutes.toDouble() / 60.0 + seconds.toDouble() / 3600.0
            if (direction.equals("S", ignoreCase = true) || direction.equals("W", ignoreCase = true)) {
                result = -result
            }
            logger.debug { "Parsed DMS with direction: $degrees° $minutes' $seconds\" $direction = $result" }
            return result
        }

        // DMS format with º symbol: "47º 8' 37.51\"" or similar
        val dmsPatternWithDegreeSymbol = Regex("""(\d+)º\s*(\d+)['′]?\s*(\d+(?:\.\d+)?)["″]?""")
        dmsPatternWithDegreeSymbol.find(coordStr)?.let {
            val (degrees, minutes, seconds) = it.destructured
            val result = degrees.toDouble() + minutes.toDouble() / 60.0 + seconds.toDouble() / 3600.0
            logger.debug { "Parsed DMS with º symbol: $degrees° $minutes' $seconds\" = $result" }
            return result
        }

        // DMS format: "52° 13' 26.43\"" or similar
        val dmsPattern = Regex("""(\d+(?:\.\d+)?)°\s*(\d+(?:\.\d+)?)['′]?\s*(\d+(?:\.\d+)?)["″]?""")
        dmsPattern.find(coordStr)?.let {
            val (degrees, minutes, seconds) = it.destructured
            val result = degrees.toDouble() + minutes.toDouble() / 60.0 + seconds.toDouble() / 3600.0
            logger.debug { "Parsed DMS format: $degrees° $minutes' $seconds\" = $result" }
            return result
        }

        // DM format: "52° 13.456'" or similar
        val dmPattern = Regex("""(\d+(?:\.\d+)?)°\s*(\d+(?:\.\d+)?)['′]""")
        dmPattern.find(coordStr)?.let {
            val (degrees, minutes) = it.destructured
            val result = degrees.toDouble() + minutes.toDouble() / 60.0
            logger.debug { "Parsed DM format: $degrees° $minutes' = $result" }
            return result
        }

        // Decimal degrees with symbol: "52.123456°"
        val decimalDegPattern = Regex("""(-?\d+(?:\.\d+)?)°""")
        decimalDegPattern.find(coordStr)?.let {
            val result = it.groupValues[1].toDouble()
            logger.debug { "Parsed decimal degrees with symbol: $result°" }
            return result
        }

        // Text format: "52 deg 13 min 26.43 sec"
        val textPattern = Regex("""(\d+(?:\.\d+)?)\s*deg(?:rees?)?\s*(\d+(?:\.\d+)?)\s*min(?:utes?)?\s*(\d+(?:\.\d+)?)\s*sec(?:onds?)?""", RegexOption.IGNORE_CASE)
        textPattern.find(coordStr)?.let {
            val (degrees, minutes, seconds) = it.destructured
            val result = degrees.toDouble() + minutes.toDouble() / 60.0 + seconds.toDouble() / 3600.0
            logger.debug { "Parsed text format: $degrees deg $minutes min $seconds sec = $result" }
            return result
        }

        // Space-separated DMS: "52 13 26.43" (assumes positive, no direction)
        val spaceDmsPattern = Regex("""^(\d+(?:\.\d+)?)\s+(\d+(?:\.\d+)?)\s+(\d+(?:\.\d+)?)$""")
        spaceDmsPattern.find(coordStr)?.let {
            val (degrees, minutes, seconds) = it.destructured
            val result = degrees.toDouble() + minutes.toDouble() / 60.0 + seconds.toDouble() / 3600.0
            logger.debug { "Parsed space-separated DMS: $degrees $minutes $seconds = $result" }
            return result
        }

        // Rational values like "47/1, 8/1, 3751/100" (assumes positive, no direction)
        val rationalPattern = Regex("""(\d+)/(\d+),\s*(\d+)/(\d+),\s*(\d+)/(\d+)""")
        rationalPattern.find(coordStr)?.let {
            val (degNum, degDenom, minNum, minDenom, secNum, secDenom) = it.destructured
            val degrees = degNum.toDouble() / degDenom.toDouble()
            val minutes = minNum.toDouble() / minDenom.toDouble()
            val seconds = secNum.toDouble() / secDenom.toDouble()
            val result = degrees + minutes / 60.0 + seconds / 3600.0
            logger.debug { "Parsed rational format: $degrees° $minutes' $seconds\" = $result" }
            return result
        }

        // Plain decimal number
        val decimalPattern = Regex("""(-?\d+(?:\.\d+)?)""")
        decimalPattern.find(coordStr)?.let {
            val result = it.groupValues[1].toDouble()
            logger.debug { "Parsed plain decimal number: $result" }
            return result
        }

        logger.warn { "Could not parse coordinate: $coordStr" }
        return null
    }
}