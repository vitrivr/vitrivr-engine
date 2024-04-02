package org.vitrivr.engine.base.features.external.implementations

import org.vitrivr.engine.base.features.external.common.ExternalFesAnalyzer.Companion.HOST_PARAMETER_DEFAULT
import org.vitrivr.engine.base.features.external.common.ExternalFesAnalyzer.Companion.HOST_PARAMETER_NAME
import org.vitrivr.engine.base.features.external.implementations.DenseEmbedding.Companion.MODEL_PARAMETER_DEFAULT
import org.vitrivr.engine.base.features.external.implementations.DenseEmbedding.Companion.MODEL_PARAMETER_NAME
import org.vitrivr.engine.core.features.AbstractExtractor
import org.vitrivr.engine.core.model.content.ContentType
import org.vitrivr.engine.core.model.content.element.ContentElement
import org.vitrivr.engine.core.model.content.element.ImageContent
import org.vitrivr.engine.core.model.descriptor.vector.FloatVectorDescriptor
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.model.retrievable.attributes.ContentAttribute
import org.vitrivr.engine.core.operators.Operator

class DenseEmbeddingExtractor(
        input: Operator<Retrievable>,
        field: Schema.Field<ContentElement<*>, FloatVectorDescriptor>,
        persisting: Boolean = true
) : AbstractExtractor<ContentElement<*>, FloatVectorDescriptor>(input, field, persisting, 1) {

    private val host: String = field.parameters[HOST_PARAMETER_NAME] ?: HOST_PARAMETER_DEFAULT
    private val model: String = field.parameters[MODEL_PARAMETER_NAME] ?: MODEL_PARAMETER_DEFAULT
    override fun matches(retrievable: Retrievable): Boolean =
            retrievable.filteredAttributes(ContentAttribute::class.java).any { it.type == ContentType.BITMAP_IMAGE }

    override fun extract(retrievable: Retrievable): List<FloatVectorDescriptor> {
//        check(retrievable is RetrievableWithContent) { "Incoming retrievable is not a retrievable with content. This is a programmer's error!" }
        val content = retrievable.filteredAttributes(ContentAttribute::class.java).map { it.content }
                .filterIsInstance<ImageContent>()
        return content.map { c ->
            (this.field.analyser as DenseEmbedding).analyse(c, this.model, this.host)
                    .copy(retrievableId = retrievable.id, transient = !this.persisting)
        }
    }
}