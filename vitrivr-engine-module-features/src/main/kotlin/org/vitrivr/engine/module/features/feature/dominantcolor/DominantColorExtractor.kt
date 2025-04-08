package org.vitrivr.engine.module.features.feature.dominantcolor

import org.vitrivr.engine.core.features.AbstractExtractor
import org.vitrivr.engine.core.features.metadata.source.file.FileSourceMetadataExtractor
import org.vitrivr.engine.core.model.content.ContentType
import org.vitrivr.engine.core.model.content.element.ImageContent
import org.vitrivr.engine.core.model.descriptor.struct.LabelDescriptor
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.core.operators.ingest.Extractor
import org.vitrivr.engine.core.source.file.FileSource

class DominantColorExtractor : AbstractExtractor<ImageContent, LabelDescriptor> {

    constructor(input: Operator<Retrievable>, analyser: DominantColor, field: Schema.Field<ImageContent, LabelDescriptor>): super(input, analyser, field)
    constructor(input: Operator<Retrievable>, analyser: DominantColor, name: String): super(input, analyser, name)

    /**
     * Internal method to check, if [Retrievable] matches this [Extractor] and should thus be processed.
     *
     * [FileSourceMetadataExtractor] implementation only works with [Retrievable] that contain a [FileSource].
     *
     * @param retrievable The [Retrievable] to check.
     * @return True on match, false otherwise,
     */
    override fun matches(retrievable: Retrievable): Boolean = retrievable.content.any { it.type == ContentType.BITMAP_IMAGE }

    override fun extract(retrievable: Retrievable)
        = (this.analyser as DominantColor).analyse(retrievable.content.filterIsInstance<ImageContent>()).map { it.copy(retrievableId = retrievable.id, field = this@DominantColorExtractor.field) }
}