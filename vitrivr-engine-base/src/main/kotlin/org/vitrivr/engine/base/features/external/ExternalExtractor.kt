package org.vitrivr.engine.base.features.external

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.vitrivr.engine.core.model.content.element.ContentElement
import org.vitrivr.engine.core.model.database.descriptor.Descriptor
import org.vitrivr.engine.core.model.database.retrievable.*
import org.vitrivr.engine.core.operators.ingest.Extractor
import java.net.http.HttpClient

/**
 * Abstract class for implementing an external feature extractor.
 *
 * @param C Type of [ContentElement] that this external extractor operates on.
 * @param D Type of [Descriptor] produced by this external extractor.
 *
 * @see [Extractor]
 *
 * @author Rahel Arnold
 * @version 1.0.0
 */
abstract class ExternalExtractor<C : ContentElement<*>, D : Descriptor> : Extractor<C, D> {


    /**
     * Creates a [Descriptor] from the given [RetrievableWithContent].
     *
     * @param retrievable The [RetrievableWithContent] from which to create the descriptor.
     * @return The generated [Descriptor].
     */
    abstract fun createDescriptor(retrievable: RetrievableWithContent): D

    /**
     * Queries the external feature extraction API for the given [RetrievableWithContent].
     *
     * @param retrievable The [RetrievableWithContent] for which to query the external feature extraction API.
     * @return A list of external features.
     */
    abstract fun queryExternalFeatureAPI(retrievable: RetrievableWithContent): List<*>

    override fun toFlow(scope: CoroutineScope): Flow<Ingested> {
        val writer = if (this.persisting) {
            this.field.getWriter()
        } else {
            null
        }
        return this.input.toFlow(scope).map { retrievable: Ingested ->
            if (retrievable is RetrievableWithContent) {
                val content = retrievable.content

                val descriptor = createDescriptor(retrievable)

                if (retrievable is RetrievableWithDescriptor.Mutable) {
                    retrievable.addDescriptor(descriptor)
                }
                writer?.add(descriptor)
            }
            retrievable
        }
    }

}
