package org.vitrivr.engine.base.features.external.common

import org.vitrivr.engine.core.features.AbstractExtractor
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
class FesExtractor<C : ContentElement<*>, D : Descriptor>(
    input: Operator<Retrievable>,
    field: Schema.Field<C, D>?,
    analyser: ExternalFesAnalyser<C, D>,
    host: String,
    model: String,
    timeoutSeconds: Long = ExternalFesAnalyser.TIMEOUTSECONDS_PARAMETER_DEFAULT,
    pollingIntervalMs: Long = ExternalFesAnalyser.POLLINGINTERVALMS_PARAMETER_DEFAULT,
    retries: Int = ExternalFesAnalyser.RETRIES_PARAMETER_DEFAULT
) : AbstractExtractor<C, D>(input, analyser, field) {

    /** [ApiWrapper] instance used by this [FesExtractor]. */
    protected val api = ApiWrapper(host, model, timeoutSeconds, pollingIntervalMs, retries)

    /**
     * Checks if the [Retrievable] matches this [Extractor] and should thus be processed.
     *
     * @param retrievable The [Retrievable] to check.
     * @return True on match, false otherwise,
     */
    override fun matches(retrievable: Retrievable): Boolean = retrievable.content.any { content ->
        this.analyser.contentClasses.any { it.isInstance(content) }
    }

    /**
     * Internal method to perform extraction on [Retrievable].
     *
     * @param retrievable The [Retrievable] to process.
     * @return List of resulting [Descriptor]s.
     */
    override fun extract(retrievable: Retrievable): List<D> = (this.analyser as ExternalFesAnalyser<C, D>).analyse(retrievable, this.api, this.field, this.field?.parameters ?: emptyMap())
}