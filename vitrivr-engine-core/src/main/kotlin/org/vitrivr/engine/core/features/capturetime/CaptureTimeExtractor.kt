package org.vitrivr.engine.core.features.capturetime

import com.drew.imaging.ImageMetadataReader
import com.drew.metadata.exif.ExifIFD0Directory
import com.drew.metadata.exif.ExifSubIFDDirectory
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
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.*

val logger = KotlinLogging.logger {}

/**
 * Extracts the capture timestamp from EXIF metadata and stores it as a
 * `Value.DateTime(LocalDateTime)`.
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


    override fun extract(r: Retrievable): List<AnyMapStructDescriptor> =
        r.content.filterIsInstance<ImageContent>().map { descriptorFor(r).copy(retrievableId = r.id, field = this.field) }


    private fun descriptorFor(r: Retrievable): AnyMapStructDescriptor {

        val src = r.filteredAttribute(SourceAttribute::class.java)?.source as? FileSource
            ?: return emptyDescriptor(r.id).also {
                logger.debug { "CaptureTimeExtractor: no FileSource for ${r.id}" }
            }

        return runCatching {
            val metadata = ImageMetadataReader.readMetadata(src.path.toFile())
            val subIFD   = metadata.getFirstDirectoryOfType(ExifSubIFDDirectory::class.java)
            val ifd0     = metadata.getFirstDirectoryOfType(ExifIFD0Directory::class.java)

            val chosen: Date? = listOfNotNull(
                subIFD?.getDateOriginal(),
                subIFD?.getDateDigitized(),
                ifd0?.getDate(ExifIFD0Directory.TAG_DATETIME)
            ).firstOrNull()

            // if no timestamp is found -> return an empty descriptor
            if (chosen == null) {
                logger.debug { "CaptureTimeExtractor: no timestamp in ${r.id}" }
                return emptyDescriptor(r.id)
            }

            val ldt: LocalDateTime =
                chosen.toInstant().atOffset(ZoneOffset.UTC).toLocalDateTime()

            logger.info { "CaptureTimeExtractor: ${r.id} → $ldt" }

            AnyMapStructDescriptor(
                UUID.randomUUID(),
                r.id,
                layout,
                mapOf("timestamp" to Value.LocalDateTimeValue(ldt)),   // uses LocalDateTime wrapper
                this.field
            )
        }.getOrElse { e ->
            logger.error(e) { "CaptureTimeExtractor: error for ${r.id}" }
            emptyDescriptor(r.id)
        }
    }

    // returns an empty descriptor
    private fun emptyDescriptor(id: UUID) = AnyMapStructDescriptor(
        UUID.randomUUID(),
        id,
        layout,
        mapOf("timestamp" to null),
        this.field
    )
}