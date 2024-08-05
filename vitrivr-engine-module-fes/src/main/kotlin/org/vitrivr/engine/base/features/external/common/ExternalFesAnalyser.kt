package org.vitrivr.engine.base.features.external.common

import org.vitrivr.engine.base.features.external.implementations.ASR
import org.vitrivr.engine.core.context.IndexContext
import org.vitrivr.engine.core.model.content.element.ContentElement
import org.vitrivr.engine.core.model.descriptor.Descriptor
import org.vitrivr.engine.core.model.metamodel.Analyser
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.core.operators.ingest.Extractor


/**
 * An [Analyser] that uses the [ApiWrapper] to analyse content.
 *
 * @param C The type of the [ContentElement] to analyse.
 * @param D The type of the [Descriptor] to generate.
 */
abstract class ExternalFesAnalyser<C : ContentElement<*>, D : Descriptor> : Analyser<C, D> {
    companion object {
        const val HOST_PARAMETER_DEFAULT = "http://localhost:8888/"
        const val HOST_PARAMETER_NAME = "host"
        const val TIMEOUTSECONDS_PARAMETER_NAME = "timeoutSeconds"
        const val TIMEOUTSECONDS_PARAMETER_DEFAULT = 20L
        const val POLLINGINTERVALMS_PARAMETER_NAME = "pollingIntervalMs"
        const val POLLINGINTERVALMS_PARAMETER_DEFAULT = 500L
        const val RETRIES_PARAMETER_NAME = "retries"
        const val RETRIES_PARAMETER_DEFAULT = 3
    }

    /** The name of the model that should be used. */
    abstract val model: String

    /**
     * Generates and returns a new [FesExtractor] instance for this [ASR].
     *
     * @param name The name of the extractor.
     * @param input The [Operator] that acts as input to the new [FesExtractor].
     * @param context The [IndexContext] to use with the [FesExtractor].
     *
     * @return A new [FesExtractor] instance for this [ASR]
     */
    override fun newExtractor(name: String, input: Operator<Retrievable>, context: IndexContext): Extractor<C, D> {
        val host = context.getProperty(name, HOST_PARAMETER_NAME) ?: HOST_PARAMETER_DEFAULT
        val timeoutSeconds = context.getProperty(name, TIMEOUTSECONDS_PARAMETER_NAME)?.toLongOrNull() ?: TIMEOUTSECONDS_PARAMETER_DEFAULT
        val pollingIntervalMs = context.getProperty(name, POLLINGINTERVALMS_PARAMETER_NAME)?.toLongOrNull() ?: POLLINGINTERVALMS_PARAMETER_DEFAULT
        val retries = context.getProperty(name, RETRIES_PARAMETER_NAME)?.toIntOrNull() ?: RETRIES_PARAMETER_DEFAULT
        return FesExtractor(input, null, this, host, this.model, timeoutSeconds, pollingIntervalMs, retries)
    }

    /**
     * Generates and returns a new [FesExtractor] instance for this [ASR].
     *
     * @param field The [Schema.Field] to create an [FesExtractor] for.
     * @param input The [Operator] that acts as input to the new [FesExtractor].
     * @param context The [IndexContext] to use with the [FesExtractor].
     *
     * @return A new [FesExtractor] instance for this [ExternalFesAnalyser]
     */
    override fun newExtractor(field: Schema.Field<C, D>, input: Operator<Retrievable>, context: IndexContext): Extractor<C, D> {
        val host = field.parameters[HOST_PARAMETER_NAME] ?: HOST_PARAMETER_DEFAULT
        val timeoutSeconds = field.parameters[TIMEOUTSECONDS_PARAMETER_NAME]?.toLongOrNull() ?: TIMEOUTSECONDS_PARAMETER_DEFAULT
        val pollingIntervalMs = field.parameters[POLLINGINTERVALMS_PARAMETER_NAME]?.toLongOrNull() ?: POLLINGINTERVALMS_PARAMETER_DEFAULT
        val retries = field.parameters[RETRIES_PARAMETER_NAME]?.toIntOrNull() ?: RETRIES_PARAMETER_DEFAULT
        return FesExtractor(input, field, this, host, this.model, timeoutSeconds, pollingIntervalMs, retries)
    }

    /**
     * Performs the analysis of the given [Retrievable]s.
     *
     * @param retrievables The [Retrievable]s to analyse.
     * @param api The [ApiWrapper] to use for the analysis.
     * @param field The [Schema.Field] to perform the analysis for.
     * @param parameters The parameters to use for the analysis.
     */
    abstract fun analyse(retrievables: Retrievable, api: ApiWrapper, field: Schema.Field<C, D>? = null, parameters: Map<String, String> = emptyMap()): List<D>
}
