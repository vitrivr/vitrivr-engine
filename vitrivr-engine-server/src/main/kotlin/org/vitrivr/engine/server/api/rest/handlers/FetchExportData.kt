package org.vitrivr.engine.server.api.rest.handlers

import io.javalin.http.Context
import io.javalin.openapi.OpenApi
import io.javalin.openapi.OpenApiContent
import io.javalin.openapi.OpenApiParam
import io.javalin.openapi.OpenApiResponse
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.retrievable.RetrievableId
import org.vitrivr.engine.server.api.rest.model.ErrorStatus

const val FETCH_ROUTE_FROM_SCHEMA = "fetch/{exporter}/{retrievableId}"

@OpenApi(
    path = "/api/{schema}/fetch/{exporter}/{retrievableId}",
    summary = "Fetch previously exported data.",
    operationId = "getPreview",
    tags = ["Content"],
    pathParams = [
        OpenApiParam("schema", String::class, "The schema this operation is for."),
    OpenApiParam("exporter", String::class, "The exporter of the schema to use."),
    OpenApiParam("retrievableId", String::class, "The ID of the retrievable.")
                 ],
    responses = [
        OpenApiResponse("200", [OpenApiContent(type = "image/jpeg")]),
        OpenApiResponse("400", [OpenApiContent(ErrorStatus::class)])
    ]
)
fun fetchExportData(ctx: Context, schema: Schema) {
    val exporterName = ctx.pathParam("exporter")
    val retrievableId: RetrievableId = try {
        RetrievableId.fromString( ctx.pathParam("retrievableId"))
    } catch (e: IllegalArgumentException) {
        ctx.status(400)
        ctx.json(ErrorStatus("Retrievable ID invalid or not specified."))
        return
    }

    /* Try to resolve resolvable for retrievable ID. */
    val resolvable = schema.getExporter(exporterName)?.resolver?.resolve(retrievableId, ".jpg")
    if (resolvable == null) {
        ctx.status(404)
        ctx.json(ErrorStatus("Failed to resolve data."))
        return
    }

    /* Check if resource backing the resolvable exists. */
    if (!resolvable.exists()) {
        ctx.status(404)
        ctx.json(ErrorStatus("Data not found."))
        return
    }

    /* Return resolvable. */
    ctx.result(resolvable.openInputStream())
    ctx.contentType(resolvable.mimeType.mimeType)
}
