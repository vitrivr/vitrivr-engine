package org.vitrivr.engine.module.features.feature.huehistogram

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
import org.vitrivr.engine.module.features.feature.ehd.EHD

/**
 * An [Extractor] implementation for the [HueHistogram] analyser.
 *
 * @see [EHD]
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
class HueHistogramExtractor : AbstractExtractor<ImageContent, FloatVectorDescriptor> {

    constructor(input: Operator<Retrievable>, analyser: HueHistogram, field: Schema.Field<ImageContent, FloatVectorDescriptor>) : super(input, analyser, field)
    constructor(input: Operator<Retrievable>, analyser: HueHistogram, name: String) : super(input, analyser, name)

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
    override fun extract(retrievable: Retrievable) = retrievable.content.filterIsInstance<ImageContent>().map {
        (this.analyser as HueHistogram).analyse(it).copy(retrievableId = retrievable.id, field = this.field)
    }
}