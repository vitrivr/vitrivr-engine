package org.vitrivr.engine.server.api.rest.handlers

import io.javalin.http.Context
import io.javalin.openapi.*
import org.vitrivr.engine.core.model.metamodel.SchemaManager
import org.vitrivr.engine.server.api.rest.model.ErrorStatus

@OpenApi(
    path = "/api/schema/list",
    summary = "Lists the names of all available schemas.",
    operationId = "getListSchemas",
    tags = ["Schema Management"],
    pathParams = [],
    responses = [
        OpenApiResponse("200", [OpenApiContent(Array<String>::class)]),
        OpenApiResponse("400", [OpenApiContent(ErrorStatus::class)])
    ]
)
fun listSchemas(ctx: Context, manager: SchemaManager) {
    ctx.json(manager.listSchemas().map { it.name })
}