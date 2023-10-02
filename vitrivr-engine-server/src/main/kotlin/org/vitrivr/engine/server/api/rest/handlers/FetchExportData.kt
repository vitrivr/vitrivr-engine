package org.vitrivr.engine.server.api.rest.handlers

import io.javalin.http.Context
import io.javalin.openapi.OpenApi
import io.javalin.openapi.OpenApiContent
import io.javalin.openapi.OpenApiResponse
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.server.api.rest.model.ErrorStatus

@OpenApi(
        path = "/api/{schema}/fetch/{exportData}/{retrievable}",
        summary = "Fetch previously exported data.",
        operationId = "getPreview",
        tags = ["Content"],
        pathParams = [],
        responses = [
            OpenApiResponse("200", [OpenApiContent(type = "image/jpeg")]),
            OpenApiResponse("400", [OpenApiContent(ErrorStatus::class)])
        ]
)
fun getPreview(ctx: Context, schema: Schema, exportDataName: String, retrievableID: String) { //what should be the parameters?
    TODO()
}