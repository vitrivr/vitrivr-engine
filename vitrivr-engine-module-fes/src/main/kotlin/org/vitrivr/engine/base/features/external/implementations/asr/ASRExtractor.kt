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
class ASRExtractor : FesExtractor<AudioContent, TextDescriptor> {

    constructor(
        input: Operator<Retrievable>,
        field: Schema.Field<AudioContent, TextDescriptor>,
        analyser: ExternalFesAnalyser<AudioContent, TextDescriptor>,
        parameters: Map<String, String>
    ) : super(input, field, analyser, parameters)

    constructor(
        input: Operator<Retrievable>,
        name: String,
        analyser: ExternalFesAnalyser<AudioContent, TextDescriptor>,
        parameters: Map<String, String>
    ) : super(input, name, analyser, parameters)

    /** The [AsrApi] used to perform extraction with. */
    private val api = AsrApi(this.host, this.model, this.timeoutMs, this.pollingIntervalMs, this.retries)


    /**
     * Internal method to perform extraction on [Retrievable].
     **
     * @param retrievables The [Retrievable]s to process.
     * @return List of resulting [Descriptor]s grouped by [Retrievable].
     */
    override fun extract(retrievables: List<Retrievable>): List<List<TextDescriptor>> {
        val content = retrievables.flatMap { it.content.filterIsInstance<AudioContent>() }
        val flatResults = this.api.analyseBatched(content).map { result -> TextDescriptor(UUID.randomUUID(), null, result, this.field)}
        var index = 0
        return retrievables.map { retrievable ->
            retrievable.content.map {
                flatResults[index++].let { TextDescriptor(it.id, retrievable.id, it.value, it.field) }
            }
        }
    }
}