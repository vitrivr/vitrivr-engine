package org.vitrivr.engine.base.features.external.implementations.clip

import org.vitrivr.engine.base.features.external.ExternalAnalyser.Companion.HOST_PARAMETER_DEFAULT
import org.vitrivr.engine.base.features.external.ExternalAnalyser.Companion.HOST_PARAMETER_NAME
import org.vitrivr.engine.core.features.AbstractExtractor
import org.vitrivr.engine.core.model.content.element.ContentElement
import org.vitrivr.engine.core.model.content.element.ImageContent
import org.vitrivr.engine.core.model.descriptor.Descriptor
import org.vitrivr.engine.core.model.descriptor.vector.FloatVectorDescriptor
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.model.retrievable.decorators.RetrievableWithContent
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.core.operators.ingest.Extractor

/**
 * [CLIPExtractor] implementation of an [AbstractExtractor] for [CLIP].
 *
 * @param field Schema field for which the extractor generates descriptors.
 * @param input Operator representing the input data source.
 * @param persisting Flag indicating whether the descriptors should be persisted.
 *
 * @author Rahel Arnold
 * @version 1.1.0
 */
class CLIPExtractor(input: Operator<Retrievable>, field: Schema.Field<ContentElement<*>, FloatVectorDescriptor>, persisting: Boolean = true) : AbstractExtractor<ContentElement<*>, FloatVectorDescriptor>(input, field, persisting) {

    /** The host of the external [CLIP] service. */
    private val host: String = field.parameters[HOST_PARAMETER_NAME] ?: HOST_PARAMETER_DEFAULT

    /**
     * Internal method to check, if [Retrievable] matches this [Extractor] and should thus be processed.
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
        val content = retrievable.content.filterIsInstance<ImageContent>()
        return content.map { c -> (this.field.analyser as CLIP).analyse(c, this.host).copy(retrievableId = retrievable.id, transient = !this.persisting) }
    }
}
