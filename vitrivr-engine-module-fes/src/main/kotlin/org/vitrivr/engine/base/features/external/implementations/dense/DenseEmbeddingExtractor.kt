package org.vitrivr.engine.base.features.external.implementations.dense

import org.vitrivr.engine.base.features.external.api.AsrApi
import org.vitrivr.engine.base.features.external.api.ImageEmbeddingApi
import org.vitrivr.engine.base.features.external.api.TextEmbeddingApi
import org.vitrivr.engine.base.features.external.common.ExternalFesAnalyser
import org.vitrivr.engine.base.features.external.common.FesExtractor
import org.vitrivr.engine.core.model.content.element.ContentElement
import org.vitrivr.engine.core.model.content.element.ImageContent
import org.vitrivr.engine.core.model.content.element.TextContent
import org.vitrivr.engine.core.model.descriptor.Descriptor
import org.vitrivr.engine.core.model.descriptor.vector.FloatVectorDescriptor
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.operators.Operator
import java.util.*

/**
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
class DenseEmbeddingExtractor : FesExtractor<ContentElement<*>, FloatVectorDescriptor> {

    constructor(
        input: Operator<Retrievable>,
        field: Schema.Field<ContentElement<*>, FloatVectorDescriptor>,
        analyser: ExternalFesAnalyser<ContentElement<*>, FloatVectorDescriptor>,
        parameters: Map<String, String>
    ) : super(input, field, analyser, parameters)

    constructor(
        input: Operator<Retrievable>,
        name: String,
        analyser: ExternalFesAnalyser<ContentElement<*>, FloatVectorDescriptor>,
        parameters: Map<String, String>
    ) : super(input, name, analyser, parameters)

    /** The [AsrApi] used to perform extraction with. */
    private val textApi by lazy { TextEmbeddingApi(this.host, model, this.timeoutMs, this.pollingIntervalMs, this.retries) }

    /** The [AsrApi] used to perform extraction with. */
    private val imageApi by lazy { ImageEmbeddingApi(this.host, model, this.timeoutMs, this.pollingIntervalMs, this.retries) }

    /**
     * Internal method to perform extraction on [Retrievable].
     **
     * @param retrievable The [Retrievable] to process.
     * @return List of resulting [Descriptor]s.
     */
    override fun extract(retrievable: Retrievable): List<FloatVectorDescriptor> = retrievable.content.mapNotNull {
        val result = when (it) {
            is ImageContent -> this.imageApi.analyse(it)
            is TextContent -> this.textApi.analyse(it)
            else -> null
        }
        if (result != null) {
            FloatVectorDescriptor(UUID.randomUUID(), retrievable.id, result, this.field)
        } else {
            null
        }
    }
}