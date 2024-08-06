package org.vitrivr.engine.base.features.external.implementations.asr

import org.vitrivr.engine.base.features.external.api.AsrApi
import org.vitrivr.engine.base.features.external.common.ExternalFesAnalyser
import org.vitrivr.engine.base.features.external.common.FesExtractor
import org.vitrivr.engine.core.model.content.element.AudioContent
import org.vitrivr.engine.core.model.descriptor.Descriptor
import org.vitrivr.engine.core.model.descriptor.scalar.StringDescriptor
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
    field: Schema.Field<AudioContent, StringDescriptor>?,
    analyser: ExternalFesAnalyser<AudioContent, StringDescriptor>,
    model: String,
    parameters: Map<String, String>
) : FesExtractor<AudioContent, StringDescriptor>(input, field, analyser, model, parameters) {
    /** The [AsrApi] used to perform extraction with. */
    private val api = AsrApi(host, model, timeoutMs, pollingIntervalMs, retries)

    /**
     * Internal method to perform extraction on [Retrievable].
     **
     * @param retrievable The [Retrievable] to process.
     * @return List of resulting [Descriptor]s.
     */
    override fun extract(retrievable: Retrievable): List<StringDescriptor> {
        val content = retrievable.content.filterIsInstance<AudioContent>()
        return content.mapNotNull { audio ->
            val result = this.api.analyse(audio)
            if (result != null) {
                StringDescriptor(UUID.randomUUID(), retrievable.id, result)
            } else {
                null
            }
        }
    }
}