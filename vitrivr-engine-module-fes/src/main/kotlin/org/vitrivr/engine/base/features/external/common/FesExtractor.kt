package org.vitrivr.engine.base.features.external.common

import org.vitrivr.engine.base.features.external.common.ExternalFesAnalyser.Companion.HOST_PARAMETER_DEFAULT
import org.vitrivr.engine.base.features.external.common.ExternalFesAnalyser.Companion.HOST_PARAMETER_NAME
import org.vitrivr.engine.base.features.external.common.ExternalFesAnalyser.Companion.MODEL_PARAMETER_NAME
import org.vitrivr.engine.base.features.external.common.ExternalFesAnalyser.Companion.POLLINGINTERVAL_MS_PARAMETER_DEFAULT
import org.vitrivr.engine.base.features.external.common.ExternalFesAnalyser.Companion.POLLINGINTERVAL_MS_PARAMETER_NAME
import org.vitrivr.engine.base.features.external.common.ExternalFesAnalyser.Companion.RETRIES_PARAMETER_DEFAULT
import org.vitrivr.engine.base.features.external.common.ExternalFesAnalyser.Companion.RETRIES_PARAMETER_NAME
import org.vitrivr.engine.base.features.external.common.ExternalFesAnalyser.Companion.TIMEOUT_MS_PARAMETER_DEFAULT
import org.vitrivr.engine.core.features.AbstractBatchedExtractor
import org.vitrivr.engine.core.model.content.element.ContentElement
import org.vitrivr.engine.core.model.descriptor.Descriptor
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.core.operators.ingest.Extractor

/**
 * An abstract [Extractor] implementation that is suitable for analysers that use the external Feature Extraction Server (FES) API.
 *
 * @author Fynn Faber
 * @version 1.1.0
 */
abstract class FesExtractor<C : ContentElement<*>, D : Descriptor<*>> : AbstractBatchedExtractor<C, D> {

    protected val parameters: Map<String, String>

    constructor(
        input: Operator<Retrievable>,
        field: Schema.Field<C, D>,
        analyser: ExternalFesAnalyser<C, D>,
        parameters: Map<String, String>
    ) : super(input, analyser, field, parameters["batchSize"]?.toIntOrNull() ?: 1) {
        this.parameters = parameters
    }

    constructor(
        input: Operator<Retrievable>,
        name: String,
        analyser: ExternalFesAnalyser<C, D>,
        parameters: Map<String, String>
    ) : super(input, analyser, name, parameters["batchSize"]?.toIntOrNull() ?: 1) {
        this.parameters = parameters
    }

    protected val host: String
        get() = this.parameters[HOST_PARAMETER_NAME] ?: HOST_PARAMETER_DEFAULT

    /** Name of the model that should be used. */
    protected val model: String
        get() = this.parameters[MODEL_PARAMETER_NAME] ?: throw IllegalStateException("Model parameter not set.")

    /** */
    protected val timeoutMs: Long
        get() = this.parameters[POLLINGINTERVAL_MS_PARAMETER_NAME]?.toLongOrNull()
            ?: TIMEOUT_MS_PARAMETER_DEFAULT

    /** */
    protected val pollingIntervalMs: Long
        get() = this.parameters[POLLINGINTERVAL_MS_PARAMETER_NAME]?.toLongOrNull()
            ?: POLLINGINTERVAL_MS_PARAMETER_DEFAULT

    /** */
    protected val retries: Int
        get() = parameters[RETRIES_PARAMETER_NAME]?.toIntOrNull() ?: RETRIES_PARAMETER_DEFAULT
}