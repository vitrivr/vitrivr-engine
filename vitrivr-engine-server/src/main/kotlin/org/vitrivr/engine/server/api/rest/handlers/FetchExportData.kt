package org.vitrivr.engine.server.api.rest.handlers

import io.javalin.http.Context
import io.javalin.openapi.OpenApi
import io.javalin.openapi.OpenApiContent
import io.javalin.openapi.OpenApiResponse
import org.vitrivr.engine.core.model.database.retrievable.RetrievableId
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.server.api.rest.model.ErrorStatus

@OpenApi(
        path = "/api/{schema}/fetch/{exporter}/{retrievable}",
        summary = "Fetch previously exported data.",
        operationId = "getPreview",
        tags = ["Content"],
        pathParams = [],
        responses = [
            OpenApiResponse("200", [OpenApiContent(type = "image/jpeg")]),
            OpenApiResponse("400", [OpenApiContent(ErrorStatus::class)])
        ]
)
fun fetchExportData(ctx: Context, schema: Schema) {
    val exporterName = ctx.pathParam("exporter")
    val retrievableIdString = ctx.pathParam("retrievable")

    val retrievableId: RetrievableId
    try {
        retrievableId = RetrievableId.fromString(retrievableIdString)
    } catch (e: IllegalArgumentException) {
        ctx.status(400)
        ctx.json(ErrorStatus("Invalid retrievable ID."))
        return
    }

    val resolvable = schema.getExporter(exporterName)?.resolve(retrievableId)
    if (resolvable == null) {
        ctx.status(404)
        ctx.json(ErrorStatus("No data found."))
        return
    }

    ctx.result(resolvable.inputStream)
    ctx.contentType(resolvable.mimeType.mimeType)
}
