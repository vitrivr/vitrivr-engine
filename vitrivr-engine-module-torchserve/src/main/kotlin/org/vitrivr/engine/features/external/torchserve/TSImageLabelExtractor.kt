package org.vitrivr.engine.features.external.torchserve

import org.vitrivr.engine.core.model.content.element.ImageContent
import org.vitrivr.engine.core.model.descriptor.Descriptor
import org.vitrivr.engine.core.model.descriptor.struct.LabelDescriptor
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.features.external.torchserve.basic.TorchServeExtractor

/**
 * [TorchServeExtractor] implementation for the [TSImageLabel] analyser.
 *
 * @see [TSImageLabel]
 *
 * @author Ralph Gasser
 * @version 1.1.0
 */
class TSImageLabelExtractor private constructor(
    val threshold: Float,
    host: String,
    port: Int,
    token: String?,
    model: String,
    input: Operator<Retrievable>,
    analyser: TSImageLabel,
    field: Schema.Field<ImageContent, LabelDescriptor>? = null,
    name: String,
    transient: Boolean
) : TorchServeExtractor<ImageContent, LabelDescriptor>(host, port, token, model, input, analyser, field, name, transient) {

    constructor(threshold: Float, host: String, port: Int, token: String?, model: String, input: Operator<Retrievable>, analyser: TSImageLabel, field: Schema.Field<ImageContent, LabelDescriptor>, transient: Boolean = false) : this(
        threshold, host, port, token, model, input, analyser, field, field.fieldName, transient
    )

    constructor(threshold: Float, host: String, port: Int, token: String?, model: String, input: Operator<Retrievable>, analyser: TSImageLabel, name: String) : this(
        threshold, host, port, token, model, input, analyser, null, name, true
    )


    /**
     * Extracts [Descriptor]s from the provided [Retrievable].
     *
     * @param retrievable [Retrievable] to extract [Descriptor]s from.
     */
    override fun extract(retrievable: Retrievable): List<LabelDescriptor> = super.extract(retrievable).filter { it.confidence.value >= this.threshold }
}