package org.vitrivr.engine.server.api.rest.handlers

import io.javalin.http.Context
import io.javalin.openapi.OpenApi
import io.javalin.openapi.OpenApiContent
import io.javalin.openapi.OpenApiResponse
import org.vitrivr.engine.server.api.rest.model.ErrorStatus

@OpenApi(
        path = "/api/{schema}/preview/{uuid}",
        summary = "Return the preview of a retrievable.",
        operationId = "getPreview",
        tags = ["Content"],
        pathParams = [],
        responses = [
            OpenApiResponse("200", [OpenApiContent(type = "image/jpeg")]),
            OpenApiResponse("400", [OpenApiContent(ErrorStatus::class)])
        ]
)
fun getPreview() { //what should be the parameters?
    TODO()
}