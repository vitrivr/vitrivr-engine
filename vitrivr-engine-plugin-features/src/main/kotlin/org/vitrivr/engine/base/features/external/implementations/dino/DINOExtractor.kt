package org.vitrivr.engine.base.features.external.implementations.dino

import org.vitrivr.engine.base.features.external.ExternalAnalyser
import org.vitrivr.engine.core.features.AbstractExtractor
import org.vitrivr.engine.core.model.content.ContentType
import org.vitrivr.engine.core.model.content.element.ImageContent
import org.vitrivr.engine.core.model.descriptor.Descriptor
import org.vitrivr.engine.core.model.descriptor.vector.FloatVectorDescriptor
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.model.retrievable.attributes.ContentAttribute
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.core.operators.ingest.Extractor

/**
 * [DINOExtractor] implementation of an [AbstractExtractor] for [DINO].
 *
 * @param field Schema field for which the extractor generates descriptors.
 * @param input Operator representing the input data source.
 * @param persisting Flag indicating whether the descriptors should be persisted.
 *
 * @author Rahel Arnold
 * @version 1.1.0
 */
class DINOExtractor(
    input: Operator<Retrievable>,
    field: Schema.Field<ImageContent, FloatVectorDescriptor>,
    persisting: Boolean
) : AbstractExtractor<ImageContent, FloatVectorDescriptor>(input, field, persisting) {

    /** The host of the external [DINO] service. */
    private val host: String =
        field.parameters[ExternalAnalyser.HOST_PARAMETER_NAME] ?: ExternalAnalyser.HOST_PARAMETER_DEFAULT

    /**
     * Internal method to check, if [Retrievable] matches this [Extractor] and should thus be processed.
     *
     * @param retrievable The [Retrievable] to check.
     * @return True on match, false otherwise,
     */
    override fun matches(retrievable: Retrievable): Boolean =
        retrievable.filteredAttributes(ContentAttribute::class.java).any { it.type == ContentType.BITMAP_IMAGE }

    /**
     * Internal method to perform extraction on [Retrievable].
     **
     * @param retrievable The [Retrievable] to process.
     * @return List of resulting [Descriptor]s.
     */
    override fun extract(retrievable: Retrievable): List<FloatVectorDescriptor> {
//        check(retrievable is RetrievableWithContent) { "Incoming retrievable is not a retrievable with content. This is a programmer's error!" }
        val content = retrievable.filteredAttributes(ContentAttribute::class.java).map { it.content }
            .filterIsInstance<ImageContent>()
        return content.map { c ->
            (this.field.analyser as DINO).analyse(c, this.host)
                .copy(retrievableId = retrievable.id, transient = !this.persisting)
        }
    }
}
