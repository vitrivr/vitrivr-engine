package org.vitrivr.engine.server.api.rest.handlers

import io.javalin.http.Context
import io.javalin.openapi.*
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.server.api.rest.model.ErrorStatus

/**
 *
 * @author Ralph Gasser
 * @version 1.0
 */
@OpenApi(
    path = "/api/{schema}/index",
    methods = [HttpMethod.POST],
    summary = "Indexes an item, adding it to the defined schema.",
    operationId = "postExecuteIngest",
    tags = ["Ingest"],
    pathParams = [
        OpenApiParam("schema", type = String::class, description = "The name of the schema to execute a query for.", required = true)
    ],
    responses = [
        OpenApiResponse("200", [OpenApiContent(Any::class)]),
        OpenApiResponse("400", [OpenApiContent(ErrorStatus::class)])
    ]
)
fun executeIngest(ctx: Context, schema: Schema) {
    TODO()
}