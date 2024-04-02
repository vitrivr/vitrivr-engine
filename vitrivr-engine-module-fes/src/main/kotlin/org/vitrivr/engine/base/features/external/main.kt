package org.vitrivr.engine.base.features.external

import org.openapitools.client.infrastructure.ApiClient
import org.openapitools.client.apis.TextEmbeddingApi
import org.vitrivr.engine.core.model.metamodel.Analyser
import org.vitrivr.engine.core.util.extension.loadServiceForName


fun main(args: Array<String>) {
    val out = loadServiceForName<Analyser<*,*>>("DenseEmbedding")
    val api = TextEmbeddingApi("http://localhost:8888")
    val input = org.openapitools.client.models.TextEmbeddingInput("Hello, world!")
    val output = api.newJobApiTasksTextEmbeddingModelJobsPost("clip-vit-large-patch14", input)
    println("Hello, world!")
//    val client = TextEmbeddingClient(basePath = "http://localhost:8888", httpClient, modelName = "clip_vit_large_patch14")
//    client.extractFeatures(InMemoryTextContent("Hello, world!") as ContentElement<Any>)

}
