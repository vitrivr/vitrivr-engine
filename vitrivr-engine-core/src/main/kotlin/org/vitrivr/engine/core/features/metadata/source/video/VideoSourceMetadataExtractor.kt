package org.vitrivr.engine.core.features.metadata.source.video

import org.vitrivr.engine.core.features.AbstractExtractor
import org.vitrivr.engine.core.features.metadata.source.file.FileSourceMetadataExtractor
import org.vitrivr.engine.core.model.content.element.ContentElement
import org.vitrivr.engine.core.model.descriptor.Descriptor
import org.vitrivr.engine.core.model.descriptor.struct.metadata.source.VideoSourceMetadataDescriptor
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.retrievable.Ingested
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.model.retrievable.attributes.SourceAttribute
import org.vitrivr.engine.core.model.types.Value
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.core.operators.ingest.Extractor
import org.vitrivr.engine.core.source.MediaType
import java.util.*

/**
 * An [Extractor] that extracts [VideoSourceMetadataDescriptor]s from [Ingested] objects.
 *
 * @author Ralph Gasser
 * @version 1.1.0
 */
class VideoSourceMetadataExtractor :
    AbstractExtractor<ContentElement<*>, VideoSourceMetadataDescriptor> {

        constructor(input: Operator<Retrievable>, analyser: VideoSourceMetadata, field: Schema.Field<ContentElement<*>, VideoSourceMetadataDescriptor>): super(input, analyser, field)
        constructor(input: Operator<Retrievable>, analyser: VideoSourceMetadata, name: String): super(input, analyser, name)

    /**
     * Internal method to check, if [Retrievable] matches this [Extractor] and should thus be processed.
     *
     * [FileSourceMetadataExtractor] implementation only works with [Retrievable] that contain a [SourceAttribute].
     *
     * @param retrievable The [Retrievable] to check.
     * @return True on match, false otherwise,
     */
    override fun matches(retrievable: Retrievable): Boolean = retrievable.filteredAttribute(SourceAttribute::class.java)?.source?.type == MediaType.VIDEO

    /**
     * Internal method to perform extraction on [Retrievable].
     **
     * @param retrievable The [Retrievable] to process.
     * @return List of resulting [Descriptor]s.
     */
    override fun extract(retrievable: Retrievable): List<VideoSourceMetadataDescriptor> {
        val source = retrievable.filteredAttribute(SourceAttribute::class.java)?.source ?: throw IllegalArgumentException("Incoming retrievable is not a retrievable with source. This is a programmer's error!")
        check(source.type == MediaType.VIDEO) { "Incoming retrievable is not a retrievable with video source. This is a programmer's error!" }
        return listOf(
            VideoSourceMetadataDescriptor(
                id = UUID.randomUUID(),
                retrievableId = retrievable.id,
                mapOf(
                    "width" to Value.Int(source.width() ?: 0),
                    "height" to Value.Int(source.height() ?: 0),
                    "duration" to Value.Long(source.duration() ?: 0L),
                    "fps" to Value.Double(source.fps() ?: 0.0),
                    "channels" to Value.Int(source.channels() ?: 0),
                    "sampleRate" to Value.Int(source.sampleRate() ?: 0),
                    "sampleSize" to Value.Int(source.sampleSize() ?: 0)
                ),
                this@VideoSourceMetadataExtractor.field
            )
        )
    }
}