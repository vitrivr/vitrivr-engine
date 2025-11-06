package org.vitrivr.engine.module.features.feature.external.implementations.emotions

import org.vitrivr.engine.core.features.AbstractExtractor
import org.vitrivr.engine.core.model.content.ContentType
import org.vitrivr.engine.core.model.content.element.ContentElement
import org.vitrivr.engine.core.model.descriptor.vector.FloatVectorDescriptor
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.core.model.content.element.AudioContent


class EmotionsSoundExtractor : AbstractExtractor<ContentElement<*>, FloatVectorDescriptor>{


    private val host: String

    constructor(
        input: Operator<out Retrievable>,
        analyser: EmotionsSound,
        field: Schema.Field<ContentElement<*>, FloatVectorDescriptor>,
        host: String
    ) : super(input, analyser, field) {
        this.host = host
    }

    constructor(
        input: Operator<out Retrievable>,
        analyser: EmotionsSound,
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
     * Performs Emotion sound extraction on all [AudioContent] elements within a [Retrievable].
     */
    override fun extract(retrievable: Retrievable) =
        retrievable.content
            .filterIsInstance<AudioContent>()
            .map { audioContent ->
                // Send base64 audio (data:audio/wav;base64,...) to /extract/emotions_sound
                EmotionsSound.analyse(audioContent, this.host)
                    .copy(
                        retrievableId = retrievable.id,
                        field = this@EmotionsSoundExtractor.field
                    )
            }
}