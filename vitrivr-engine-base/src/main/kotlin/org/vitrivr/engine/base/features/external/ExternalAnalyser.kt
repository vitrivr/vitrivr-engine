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
 * @property host The host address of the external feature extraction service.
 * @property port The port of the external feature extraction service.
 * @property endpoint The endpoint of the external feature to extract.
 *
 * @see [Analyser]
 *
 * @author Rahel Arnold
 * @version 1.0.0
 */
abstract class ExternalAnalyser<T : ContentElement<*>, U : Descriptor> : Analyser<T, U> {
    /** The host address of the external feature extraction service. */
    abstract val host: String

    /** The port of the external feature extraction service. */
    abstract val port: Int

    /** The endpoint for the external feature extraction service. */
    abstract val endpoint: String

    /**
     * Requests the external feature descriptor for the given [ContentElement].
     *
     * @param content The [ContentElement] for which to request the external feature descriptor.
     * @return A list of external feature descriptors.
     */
    abstract fun requestDescriptor(content: ContentElement<*>): List<*>
}
