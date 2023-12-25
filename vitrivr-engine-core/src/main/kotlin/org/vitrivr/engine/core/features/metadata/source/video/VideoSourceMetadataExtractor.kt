package org.vitrivr.engine.core.features.metadata.source.video

import org.vitrivr.engine.core.features.AbstractExtractor
import org.vitrivr.engine.core.features.metadata.source.file.FileSourceMetadataExtractor
import org.vitrivr.engine.core.model.content.element.ContentElement
import org.vitrivr.engine.core.model.descriptor.Descriptor
import org.vitrivr.engine.core.model.descriptor.struct.metadata.source.VideoSourceMetadataDescriptor
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.model.retrievable.decorators.RetrievableWithSource
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.core.operators.ingest.Extractor
import org.vitrivr.engine.core.source.MediaType
import org.vitrivr.engine.core.source.file.FileSource
import java.util.*

/**
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
class VideoSourceMetadataExtractor(
    input: Operator<Retrievable>,
    field: Schema.Field<ContentElement<*>, VideoSourceMetadataDescriptor>,
    persisting: Boolean = true
) : AbstractExtractor<ContentElement<*>, VideoSourceMetadataDescriptor>(input, field, persisting, bufferSize = 1) {
    /**
     * Internal method to check, if [Retrievable] matches this [Extractor] and should thus be processed.
     *
     * [FileSourceMetadataExtractor] implementation only works with [RetrievableWithSource] that contain a [FileSource].
     *
     * @param retrievable The [Retrievable] to check.
     * @return True on match, false otherwise,
     */
    override fun matches(retrievable: Retrievable): Boolean =
        retrievable is RetrievableWithSource && retrievable.source.type == MediaType.VIDEO

    /**
     * Internal method to perform extraction on [Retrievable].
     **
     * @param retrievable The [Retrievable] to process.
     * @return List of resulting [Descriptor]s.
     */
    override fun extract(retrievable: Retrievable): List<VideoSourceMetadataDescriptor> {
        check(retrievable is RetrievableWithSource) { "Incoming retrievable is not a retrievable with source. This is a programmer's error!" }
        check(retrievable.source.type == MediaType.VIDEO) { "Incoming retrievable is not a retrievable with video source. This is a programmer's error!" }
        return listOf(
            VideoSourceMetadataDescriptor(
                id = UUID.randomUUID(),
                retrievableId = retrievable.id,
                width = retrievable.source.width() ?: 0,
                height = retrievable.source.height() ?: 0,
                fps = retrievable.source.fps() ?: 0.0,
                channels = retrievable.source.channels() ?: 0,
                sampleRate = retrievable.source.sampleRate() ?: 0,
                sampleSize = retrievable.source.sampleSize() ?: 0,
                transient = !persisting
            )
        )
    }
}