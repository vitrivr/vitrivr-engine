package org.vitrivr.engine.base.features.external.common

import kotlinx.coroutines.delay
import org.openapitools.client.apis.ImageEmbeddingApi
import org.openapitools.client.apis.TextEmbeddingApi
import org.openapitools.client.models.ImageEmbeddingInput
import org.openapitools.client.models.ImageEmbeddingOutput
import org.openapitools.client.models.TextEmbeddingInput
import org.openapitools.client.models.TextEmbeddingOutput
import org.vitrivr.engine.core.model.content.element.ContentElement
import org.vitrivr.engine.core.model.descriptor.Descriptor
import org.vitrivr.engine.core.model.metamodel.Analyser



abstract class ExternalFesAnalyzer<T : ContentElement<*>, U : Descriptor>: Analyser<T, U> {
    companion object {
        const val HOST_PARAMETER_DEFAULT = "http://localhost:8888/"
        const val HOST_PARAMETER_NAME = "host"
    }



    fun analyse(content: T, model: String, hostName: String): U {
        val apiWrapper = ApiWrapper(hostName, model)
        return analyse(content, apiWrapper)
    }

    abstract fun analyse(content: T, apiWrapper: ApiWrapper): U



}