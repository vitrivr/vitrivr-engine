package org.vitrivr.engine.base.features.external.implementations.asr

import org.vitrivr.engine.base.features.external.api.AsrApi
import org.vitrivr.engine.base.features.external.common.ExternalFesAnalyser
import org.vitrivr.engine.base.features.external.common.FesExtractor
import org.vitrivr.engine.core.model.content.element.AudioContent
import org.vitrivr.engine.core.model.descriptor.Descriptor
import org.vitrivr.engine.core.model.descriptor.scalar.TextDescriptor
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.operators.Operator
import java.util.*

/**
 * A [FesExtractor] to perform [ASR] on [AudioContent].
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
class ASRExtractor(
    input: Operator<Retrievable>,
    field: Schema.Field<AudioContent, TextDescriptor>?,
    analyser: ExternalFesAnalyser<AudioContent, TextDescriptor>,
    parameters: Map<String, String>
) : FesExtractor<AudioContent, TextDescriptor>(input, field, analyser, parameters) {
    /** The [AsrApi] used to perform extraction with. */
    private val api = AsrApi(this.host, this.model, this.timeoutMs, this.pollingIntervalMs, this.retries)

    /**
     * Internal method to perform extraction on [Retrievable].
     **
     * @param retrievable The [Retrievable] to process.
     * @return List of resulting [Descriptor]s.
     */
    override fun extract(retrievable: Retrievable): List<TextDescriptor> {
        return this.filterContent(retrievable).mapNotNull { audio ->
            val result = this.api.analyse(audio)
            if (result != null) {
                TextDescriptor(UUID.randomUUID(), retrievable.id, result, this.field)
            } else {
                null
            }
        }
    }
}