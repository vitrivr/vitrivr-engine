package org.vitrivr.engine.base.features.external.implementations.clip.text

import org.vitrivr.engine.core.features.AbstractExtractor
import org.vitrivr.engine.core.features.metadata.file.FileMetadataExtractor
import org.vitrivr.engine.core.model.content.element.TextContent
import org.vitrivr.engine.core.model.descriptor.Descriptor
import org.vitrivr.engine.core.model.descriptor.vector.FloatVectorDescriptor
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.model.retrievable.decorators.RetrievableWithContent
import org.vitrivr.engine.core.model.retrievable.decorators.RetrievableWithSource
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.core.operators.ingest.Extractor
import org.vitrivr.engine.core.source.file.FileSource

/**
 * [CLIPTextExtractor] implementation of an [AbstractExtractor] for [CLIPText].
 *
 * @param field Schema field for which the extractor generates descriptors.
 * @param input Operator representing the input data source.
 * @param persisting Flag indicating whether the descriptors should be persisted.
 *
 * @author Rahel Arnold
 * @version 1.0.0
 */
class CLIPTextExtractor(input: Operator<Retrievable>, field: Schema.Field<TextContent, FloatVectorDescriptor>, persisting: Boolean) : AbstractExtractor<TextContent, FloatVectorDescriptor>(input, field, persisting) {
    /**
     * Internal method to check, if [Retrievable] matches this [Extractor] and should thus be processed.
     *
     * [FileMetadataExtractor] implementation only works with [RetrievableWithSource] that contain a [FileSource].
     *
     * @param retrievable The [Retrievable] to check.
     * @return True on match, false otherwise,
     */
    override fun matches(retrievable: Retrievable): Boolean = retrievable is RetrievableWithContent

    /**
     * Internal method to perform extraction on [Retrievable].
     **
     * @param retrievable The [Retrievable] to process.
     * @return List of resulting [Descriptor]s.
     */
    override fun extract(retrievable: Retrievable): List<FloatVectorDescriptor> {
        check(retrievable is RetrievableWithContent) { "Incoming retrievable is not a retrievable with content. This is a programmer's error!" }
        val content = retrievable.content.filterIsInstance<TextContent>()
        return content.map { c -> FloatVectorDescriptor(retrievableId = retrievable.id, vector = CLIPText.requestDescriptor(c), transient = !this.persisting) }
    }
}
