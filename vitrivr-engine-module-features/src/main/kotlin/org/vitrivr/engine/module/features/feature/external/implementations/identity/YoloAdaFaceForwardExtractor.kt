package org.vitrivr.engine.module.features.feature.external.implementations.identity

import org.vitrivr.engine.core.features.AbstractExtractor
import org.vitrivr.engine.core.model.content.element.ImageContent
import org.vitrivr.engine.core.model.descriptor.struct.LabelDescriptor
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.model.retrievable.attributes.SourceAttribute
import org.vitrivr.engine.core.source.MediaType
import org.vitrivr.engine.core.operators.Operator

class YoloAdaFaceForwardExtractor : AbstractExtractor<ImageContent, LabelDescriptor> {
    private val host: String
    constructor(input: Operator<out Retrievable>, analyser: YoloAdaFaceForward, field: Schema.Field<ImageContent, LabelDescriptor>, host: String) : super(input, analyser, field) { this.host = host }
    constructor(input: Operator<out Retrievable>, analyser: YoloAdaFaceForward, name: String, host: String) : super(input, analyser, name) { this.host = host }

    override fun matches(retrievable: Retrievable) =
        retrievable.type == VIDEO_END_TYPE || super.matches(retrievable)

    override fun extract(retrievable: Retrievable): List<LabelDescriptor> {
        val source = retrievable.filteredAttribute(SourceAttribute::class.java)?.source
        val streamId = (source?.sourceId ?: retrievable.id).toString()

        if (retrievable.type == VIDEO_END_TYPE) {
            YoloAdaFaceForward.reset(host, streamId)
            return emptyList()
        }

        val labels = retrievable.content.filterIsInstance<ImageContent>()
            .flatMap { YoloAdaFaceForward.analyse(it, host, streamId) }
            .map { it.copy(retrievableId = retrievable.id, field = field) }

        /* Still images have no terminal video signal, so they must be reset immediately. */
        if (source?.type != MediaType.VIDEO) YoloAdaFaceForward.reset(host, streamId)
        return labels
    }

    private companion object {
        const val VIDEO_END_TYPE = "SOURCE:VIDEO"
    }
}
