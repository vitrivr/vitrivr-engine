package org.vitrivr.engine.server.api.rest.handlers

import io.javalin.http.Context
import io.javalin.openapi.*
import kotlinx.serialization.Serializable
import org.vitrivr.engine.core.model.metamodel.SchemaManager
import org.vitrivr.engine.server.api.rest.model.ErrorStatus


@Serializable
data class SchemaList(val schemas: List<String>)

@OpenApi(
    path = "/api/schema/list",
    summary = "Lists the names of all available schemas.",
    operationId = "getListSchemas",
    tags = ["Schema Management"],
    pathParams = [],
    responses = [
        OpenApiResponse("200", [OpenApiContent(SchemaList::class)]),
        OpenApiResponse("400", [OpenApiContent(ErrorStatus::class)])
    ]
)
fun listSchemas(ctx: Context, manager: SchemaManager) {
    ctx.json(SchemaList(manager.listSchemas().map { it.name }))
}