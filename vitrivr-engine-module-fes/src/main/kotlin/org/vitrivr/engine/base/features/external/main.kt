package org.vitrivr.engine.base.features.external

import org.openapitools.client.infrastructure.ApiClient
import org.openapitools.client.apis.TextEmbeddingApi


fun main(args: Array<String>) {
    val api = TextEmbeddingApi("http://localhost:8888")
    val input = org.openapitools.client.models.TextEmbeddingInput("Hello, world!")
    val output = api.newJobApiTasksTextEmbeddingModelJobsPost("clip-vit-large-patch14", input)
    println("Hello, world!")
//    val client = TextEmbeddingClient(basePath = "http://localhost:8888", httpClient, modelName = "clip_vit_large_patch14")
//    client.extractFeatures(InMemoryTextContent("Hello, world!") as ContentElement<Any>)

}
