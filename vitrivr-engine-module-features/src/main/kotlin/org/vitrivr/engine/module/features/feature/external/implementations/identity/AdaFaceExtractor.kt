package org.vitrivr.engine.module.features.feature.external.implementations.identity

import org.vitrivr.engine.core.features.AbstractExtractor
import org.vitrivr.engine.core.model.content.element.ImageContent
import org.vitrivr.engine.core.model.descriptor.struct.LabelDescriptor
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.operators.Operator

class AdaFaceExtractor : AbstractExtractor<ImageContent, LabelDescriptor> {
    private val host: String
    constructor(input: Operator<out Retrievable>, analyser: AdaFace, field: Schema.Field<ImageContent, LabelDescriptor>, host: String) : super(input, analyser, field) { this.host = host }
    constructor(input: Operator<out Retrievable>, analyser: AdaFace, name: String, host: String) : super(input, analyser, name) { this.host = host }

    override fun extract(retrievable: Retrievable) = retrievable.content.filterIsInstance<ImageContent>()
        .flatMap { AdaFace.analyse(it, host) }
        .map { it.copy(retrievableId = retrievable.id, field = field) }
}
