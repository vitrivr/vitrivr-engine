package org.vitrivr.engine.base.features.external.common

import org.vitrivr.engine.base.features.external.api.AbstractApi
import org.vitrivr.engine.core.model.content.element.ContentElement
import org.vitrivr.engine.core.model.descriptor.Descriptor
import org.vitrivr.engine.core.model.metamodel.Analyser


/**
 * An [Analyser] that uses the [AbstractApi] to analyse content.
 *
 * @param C The type of the [ContentElement] to analyse.
 * @param D The type of the [Descriptor] to generate.
 */
abstract class ExternalFesAnalyser<C : ContentElement<*>, D : Descriptor> : Analyser<C, D> {
    companion object {
        const val HOST_PARAMETER_DEFAULT = "http://localhost:8888/"
        const val HOST_PARAMETER_NAME = "host"
        const val TIMEOUT_MS_PARAMETER_NAME = "timeoutMs"
        const val TIMEOUT_MS_PARAMETER_DEFAULT = 20000L
        const val POLLINGINTERVAL_MS_PARAMETER_NAME = "pollingIntervalMs"
        const val POLLINGINTERVAL_MS_PARAMETER_DEFAULT = 1000L
        const val RETRIES_PARAMETER_NAME = "retries"
        const val RETRIES_PARAMETER_DEFAULT = 3
    }

    /** The name of the model that should be used. */
    abstract val model: String
}
