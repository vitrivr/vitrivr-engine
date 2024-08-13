package org.vitrivr.engine.module.features.feature.external.implementations.clip

import org.vitrivr.engine.core.features.AbstractExtractor
import org.vitrivr.engine.core.model.content.ContentType
import org.vitrivr.engine.core.model.content.element.ContentElement
import org.vitrivr.engine.core.model.content.element.ImageContent
import org.vitrivr.engine.core.model.descriptor.Descriptor
import org.vitrivr.engine.core.model.descriptor.vector.FloatVectorDescriptor
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.core.operators.ingest.Extractor

/**
 * [CLIPExtractor] implementation of an [AbstractExtractor] for [CLIP].
 *
 * @param field Schema field for which the extractor generates descriptors.
 * @param input Operator representing the input data source.
 *
 * @author Rahel Arnold
 * @version 1.3.0
 */
class CLIPExtractor(input: Operator<Retrievable>, analyser: CLIP, field: Schema.Field<ContentElement<*>, FloatVectorDescriptor>?, private val host: String) : AbstractExtractor<ContentElement<*>, FloatVectorDescriptor>(input, analyser, field) {
    /**
     * Internal method to check, if [Retrievable] matches this [Extractor] and should thus be processed.
     *
     * @param retrievable The [Retrievable] to check.
     * @return True on match, false otherwise,
     */
    override fun matches(retrievable: Retrievable): Boolean = retrievable.content.any { it.type == ContentType.BITMAP_IMAGE }

    /**
     * Internal method to perform extraction on [Retrievable].
     **
     * @param retrievable The [Retrievable] to process.
     * @return List of resulting [Descriptor]s.
     */
    override fun extract(retrievable: Retrievable): List<FloatVectorDescriptor> {
        val content = retrievable.content.filterIsInstance<ImageContent>()
        return content.map { c ->
            CLIP.analyse(c, this.host).copy(retrievableId = retrievable.id, field = this@CLIPExtractor.field, sourceName = CLIPExtractor::class.java.simpleName)
        }
    }
}
