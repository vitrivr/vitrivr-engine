package org.vitrivr.engine.base.features.external

import org.vitrivr.engine.core.model.content.element.ContentElement
import org.vitrivr.engine.core.model.descriptor.Descriptor
import org.vitrivr.engine.core.model.metamodel.Analyser

/**
 * Implementation of the [ExternalAnalyser], which derives external features from an [ContentElement] as [Descriptor].
 *
 * @param T Type of [ContentElement] that this external analyzer operates on.
 * @param U Type of [Descriptor] produced by this external analyzer.
 *
 * @see [Analyser]
 *
 * @author Rahel Arnold
 * @version 1.1.0
 */
abstract class ExternalAnalyser<T : ContentElement<*>, U : Descriptor> : Analyser<T, U> {
    companion object {
        /** Name of the host parameter */
        const val HOST_PARAMETER_NAME = "host"

        /** Default value of the grid_size parameter. */
        const val HOST_PARAMETER_DEFAULT = "http://localhost:8888/"
    }

    /**
     * Analyses a [ContentElement] using this [ExternalAnalyser] and returns a list of [Descriptor]s.
     *
     * @param content The [ContentElement] for which to request the external feature descriptor.
     * @param hostname The hostname of the external feature descriptor service.
     * @return A list of external feature [Descriptor] of type [U]
     */
    abstract fun analyse(content: T, hostname: String = HOST_PARAMETER_DEFAULT): U
}
