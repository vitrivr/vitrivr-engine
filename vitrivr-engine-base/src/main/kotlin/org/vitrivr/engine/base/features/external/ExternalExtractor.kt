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
import java.net.http.HttpRequest
import java.net.http.HttpResponse

/**
 * [Extractor] implementation for external feature extraction.
 *
 * @see [ExternalFeatureExtractor]
 *
 * @author Rahel Arnold
 * @version 1.0.0
 */
abstract class ExternalExtractor<C : ContentElement<*>, D : Descriptor>(
    override val persisting: Boolean = true,
    private val host: String,
    private val port: Int,
    private val featureName: String
) : Extractor<ContentElement<*>, Descriptor> {

    private val client = HttpClient.newHttpClient()

    abstract fun createHttpRequest(content: ContentElement<*>): HttpRequest

    abstract fun parseFeatureResponse(response: String): List<Float>

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

                /*val descriptor = FloatVectorDescriptor(
                    retrievableId = retrievable.id,
                    transient = !persisting,
                    vector = queryExternalFeatureAPI(content.first())
                )*/

                val descriptor = createDescriptor(retrievable.id, content)

                if (retrievable is RetrievableWithDescriptor.Mutable) {
                    retrievable.addDescriptor(descriptor)
                }
                writer?.add(descriptor)
            }
            retrievable
        }
    }


    private fun queryExternalFeatureAPI(content: ContentElement<*>): List<Float> {
        val request = createHttpRequest(content)
        val response = client.send(request, HttpResponse.BodyHandlers.ofString())

        // Extract and parse the response from the external feature API
        return parseFeatureResponse(response.body())
    }
}
