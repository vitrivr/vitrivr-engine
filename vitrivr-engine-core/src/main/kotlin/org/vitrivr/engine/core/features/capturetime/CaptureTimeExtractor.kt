package org.vitrivr.engine.core.features.capturetime

import com.drew.imaging.ImageMetadataReader
import com.drew.metadata.exif.ExifIFD0Directory
import com.drew.metadata.exif.ExifSubIFDDirectory
import com.drew.metadata.jpeg.JpegCommentDirectory
import io.github.oshai.kotlinlogging.KotlinLogging
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
import java.util.*

val logger = KotlinLogging.logger {}

/**
 * Extracts the capture timestamp from EXIF metadata and stores it as a [Value.DateTime]
 *
 * Priority order
 *   1. Date/Time Original
 *   2. Date/Time Digitized (most of the time this is similar to original on digital cameras)
 *   3. Date/Time (TAG_DATETIME, which usually stores time of the last modification)
 */
class CaptureTimeExtractor : AbstractExtractor<ImageContent, AnyMapStructDescriptor> {

    constructor(input: Operator<Retrievable>, analyser: CaptureTime, field: Schema.Field<ImageContent, AnyMapStructDescriptor>) : super(input, analyser, field)

    constructor(input: Operator<Retrievable>, analyser: CaptureTime, name: String) : super(input, analyser, name)

    //for what is this used?
    private val layout = listOf(Attribute("timestamp", Type.Datetime))

    // we need file access (and ContentType.BITMAP_IMAGE)
    override fun matches(r: Retrievable): Boolean = r.content.any { it.type == ContentType.BITMAP_IMAGE } && r.filteredAttribute(SourceAttribute::class.java)?.source is FileSource

    /**
     * Extracts timestamp information from each [ImageContent] element of a [Retrievable].
     *
     * @param r The [Retrievable] to process.
     * @return A list containing a single [AnyMapStructDescriptor] if extraction succeeded, or an empty list otherwise.
     */
    override fun extract(r: Retrievable): List<AnyMapStructDescriptor> {
        val descriptor = descriptorFor(r)
        return if (descriptor != null) {
            listOf(descriptor.copy(retrievableId = r.id, field = this.field))
        } else {
            emptyList()
        }
    }


    /**
     * Attempts to extract the timestamp from image metadata of a [Retrievable]'s image file.
     *
     * @param r The [Retrievable] to process.
     * @return A populated [AnyMapStructDescriptor] or `null` on failure.
     */
    private fun descriptorFor(r: Retrievable): AnyMapStructDescriptor? {

        val src = r.filteredAttribute(SourceAttribute::class.java)?.source as? FileSource
            ?: run {
                logger.debug { "CaptureTimeExtractor: no FileSource for ${r.id}. Skipping descriptor creation." }
                return null
            }

        return runCatching {
            val metadata = ImageMetadataReader.readMetadata(src.path.toFile()) // TODO figure out how to read from stream
            val subIFD = metadata.getFirstDirectoryOfType(ExifSubIFDDirectory::class.java)
            val ifd0 = metadata.getFirstDirectoryOfType(ExifIFD0Directory::class.java)

            val chosenTimestamp: Date? = listOfNotNull(
                subIFD?.getDateOriginal(),
                subIFD?.getDateDigitized(),
                ifd0?.getDate(ExifIFD0Directory.TAG_DATETIME)
            ).firstOrNull()

            // if no timestamp is found -> return null
            if (chosenTimestamp == null) {
                logger.debug { "CaptureTimeExtractor: no timestamp found in metadata for ${r.id}. Skipping descriptor creation." }
                return@runCatching null // Return null from the runCatching lambda
            }

            logger.info { "CaptureTimeExtractor: ${r.id} → $chosenTimestamp" }

            AnyMapStructDescriptor(
                UUID.randomUUID(),
                r.id,
                layout,
                mapOf("timestamp" to Value.DateTime(chosenTimestamp)), // Use Value.DateTime for java.util.Date
                this.field
            )
        }.getOrElse { e ->
            logger.error(e) { "CaptureTimeExtractor: Error during timestamp extraction for ${r.id}. Skipping descriptor creation." }
            null
        }
    }
}