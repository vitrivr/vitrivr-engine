package org.vitrivr.engine.module.features.feature.external.implementations.ocr

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Semaphore
import org.vitrivr.engine.core.features.AbstractExtractor
import org.vitrivr.engine.core.model.content.ContentType
import org.vitrivr.engine.core.model.content.element.ContentElement
import org.vitrivr.engine.core.model.content.element.ImageContent
import org.vitrivr.engine.core.model.descriptor.scalar.TextDescriptor
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.operators.Operator

/**
 * [OpticalCharacterRecognitionExtractor] implementation of an [AbstractExtractor] for [OpticalCharacterRecognition].
 *
 * This extractor sends image content to the external OCR service (our Flask/APIFlask endpoint)
 * and creates a [TextDescriptor] containing the extracted text.
 *
 * @author Rahel Arnold
 * @version 1.0.0
 */
class OpticalCharacterRecognitionExtractor(
    input: Operator<out Retrievable>,
    analyser: OpticalCharacterRecognition,
    field: Schema.Field<ContentElement<*>, TextDescriptor>?,
    name: String,
    private val host: String,
    private val concurrentRequests: Int
) : AbstractExtractor<ContentElement<*>, TextDescriptor>(input, analyser, field, name) {
    constructor(
        input: Operator<out Retrievable>, analyser: OpticalCharacterRecognition,
        field: Schema.Field<ContentElement<*>, TextDescriptor>, host: String
    ) : this(input, analyser, field, field.fieldName, host, 1)

    constructor(
        input: Operator<out Retrievable>, analyser: OpticalCharacterRecognition, name: String, host: String
    ) : this(input, analyser, null, name, host, 1)

    init {
        require(concurrentRequests in 1..32) { "concurrentRequests must be between 1 and 32." }
    }

    override fun toFlow(scope: CoroutineScope): Flow<Retrievable> {
        if (concurrentRequests == 1) return super.toFlow(scope)
        return flow {
            coroutineScope {
                val pending = Semaphore(concurrentRequests)
                input.toFlow(scope).map { item ->
                    pending.acquire()
                    async(Dispatchers.IO) { item to extractDescriptors(item) }
                }.buffer(concurrentRequests - 1).collect { request ->
                    val (item, descriptors) = request.await()
                    pending.release()
                    emit(persistDescriptors(item, descriptors))
                }
            }
        }
    }

    /**
     * Determines if this extractor can process the given [Retrievable].
     */
    override fun matches(retrievable: Retrievable): Boolean =
        retrievable.content.any { it.type == ContentType.BITMAP_IMAGE }

    /**
     * Performs OCR extraction on all [ImageContent] elements within a [Retrievable].
     */
    override fun extract(retrievable: Retrievable) =
        retrievable.content
            .filterIsInstance<ImageContent>()
            .map { imageContent ->
                OpticalCharacterRecognition.analyse(imageContent, this.host)
                    .copy(retrievableId = retrievable.id, field = this@OpticalCharacterRecognitionExtractor.field)
            }
}
