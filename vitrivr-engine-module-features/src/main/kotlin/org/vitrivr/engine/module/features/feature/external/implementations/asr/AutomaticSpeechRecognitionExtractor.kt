package org.vitrivr.engine.module.features.feature.external.implementations.asr

import org.vitrivr.engine.core.features.AbstractExtractor
import org.vitrivr.engine.core.model.content.ContentType
import org.vitrivr.engine.core.model.content.element.AudioContent
import org.vitrivr.engine.core.model.content.element.ContentElement
import org.vitrivr.engine.core.model.descriptor.scalar.TextDescriptor
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.operators.Operator

/**
 * [AutomaticSpeechRecognitionExtractor] implementation of an [AbstractExtractor] for [AutomaticSpeechRecognition].
 *
 * This extractor sends audio content to the external ASR service (our Flask/APIFlask Whisper endpoint)
 * and creates a [TextDescriptor] containing the transcribed text.
 *
 * @author Rahel Arnold
 * @version 1.0.0
 */
class AutomaticSpeechRecognitionExtractor : AbstractExtractor<ContentElement<*>, TextDescriptor> {

    private val host: String

    constructor(
        input: Operator<out Retrievable>,
        analyser: AutomaticSpeechRecognition,
        field: Schema.Field<ContentElement<*>, TextDescriptor>,
        host: String
    ) : super(input, analyser, field) {
        this.host = host
    }

    constructor(
        input: Operator<out Retrievable>,
        analyser: AutomaticSpeechRecognition,
        name: String,
        host: String
    ) : super(input, analyser, name) {
        this.host = host
    }

    /**
     * Determines if this extractor can process the given [Retrievable].
     */
    override fun matches(retrievable: Retrievable): Boolean =
        retrievable.content.any { it.type == ContentType.AUDIO_FRAME }

    /**
     * Performs ASR extraction on all [AudioContent] elements within a [Retrievable].
     */
    override fun extract(retrievable: Retrievable) =
        retrievable.content
            .filterIsInstance<AudioContent>()
            .map { audioContent ->
                // Send base64 audio (data:audio/wav;base64,...) to /extract/asr
                AutomaticSpeechRecognition.analyse(audioContent, this.host)
                    .copy(
                        retrievableId = retrievable.id,
                        field = this@AutomaticSpeechRecognitionExtractor.field
                    )
            }
}
