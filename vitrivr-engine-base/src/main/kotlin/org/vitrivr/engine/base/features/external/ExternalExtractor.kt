package org.vitrivr.engine.base.features.external

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.vitrivr.engine.core.model.content.element.ContentElement
import org.vitrivr.engine.core.model.database.descriptor.Descriptor
import org.vitrivr.engine.core.model.database.retrievable.Ingested
import org.vitrivr.engine.core.model.database.retrievable.RetrievableId
import org.vitrivr.engine.core.model.database.retrievable.RetrievableWithContent
import org.vitrivr.engine.core.model.database.retrievable.RetrievableWithDescriptor
import org.vitrivr.engine.core.operators.ingest.Extractor
import java.net.http.HttpClient

/**
 * [Extractor] implementation for external feature extraction.
 *
 * @see [ExternalFeatureExtractor]
 *
 * @author Rahel Arnold
 * @version 1.0.0
 */
abstract class ExternalExtractor<C : ContentElement<*>, D : Descriptor>(
    //override val persisting: Boolean = true,

) : Extractor<C, D> {
    abstract val host: String
    abstract val port: Int
    abstract val featureName: String

    private val client = HttpClient.newHttpClient()

    abstract fun createHttpRequest(content: ContentElement<*>): List<*>

    abstract fun createDescriptor(retrievableId: RetrievableId, content: List<ContentElement<*>>): D

    override fun toFlow(scope: CoroutineScope): Flow<Ingested> {
        val writer = if (this.persisting) {
            this.field.getWriter()
        } else {
            null
        }
        return this.input.toFlow(scope).map { retrievable: Ingested ->
            if (retrievable is RetrievableWithContent) {
                val content = retrievable.content

                val descriptor = createDescriptor(retrievable.id, content)

                if (retrievable is RetrievableWithDescriptor.Mutable) {
                    retrievable.addDescriptor(descriptor)
                }
                writer?.add(descriptor)
            }
            retrievable
        }
    }


    abstract fun queryExternalFeatureAPI(content: ContentElement<*>): List<*>
}
