package org.vitrivr.engine.module.features.feature.ehd

import org.vitrivr.engine.core.features.AbstractExtractor
import org.vitrivr.engine.core.features.metadata.source.file.FileSourceMetadataExtractor
import org.vitrivr.engine.core.model.content.ContentType
import org.vitrivr.engine.core.model.content.element.ImageContent
import org.vitrivr.engine.core.model.descriptor.Descriptor
import org.vitrivr.engine.core.model.descriptor.vector.FloatVectorDescriptor
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.core.operators.ingest.Extractor
import org.vitrivr.engine.core.source.file.FileSource

/**
 * [Extractor] implementation for the [EHD] analyser.
 *
 * @see [EHD]
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
class EHDExtractor(input: Operator<Retrievable>, analyser: EHD, field: Schema.Field<ImageContent, FloatVectorDescriptor>?) : AbstractExtractor<ImageContent, FloatVectorDescriptor>(input, analyser, field) {
    /**
     * Internal method to check, if [Retrievable] matches this [Extractor] and should thus be processed.
     *
     * [FileSourceMetadataExtractor] implementation only works with [Retrievable] that contain a [FileSource].
     *
     * @param retrievable The [Retrievable] to check.
     * @return True on match, false otherwise,
     */
    override fun matches(retrievable: Retrievable): Boolean = retrievable.content.any { it.type == ContentType.BITMAP_IMAGE }

    /**
     * Internal method to perform extraction on [Retrievable].
     *
     * @param retrievable The [Retrievable] to process.
     * @return List of resulting [Descriptor]s.
     */
    override fun extract(retrievable: Retrievable): List<FloatVectorDescriptor> {
        val content = retrievable.content.filterIsInstance<ImageContent>()
        return content.map { (this.analyser as EHD).analyse(it).copy(retrievableId = retrievable.id, field = this.field) }
    }
}