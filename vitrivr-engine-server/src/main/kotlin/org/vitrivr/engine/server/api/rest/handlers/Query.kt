package org.vitrivr.engine.server.api.rest.handlers

import io.javalin.http.Context
import io.javalin.http.bodyAsClass
import io.javalin.openapi.*
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.query.execution.RetrievalRuntime
import org.vitrivr.engine.query.model.api.InformationNeedDescription
import org.vitrivr.engine.query.model.api.result.QueryResult
import org.vitrivr.engine.server.api.rest.model.ErrorStatus
import org.vitrivr.engine.server.api.rest.model.ErrorStatusException

/**
 *
 * @author Ralph Gasser
 * @version 1.0
 */
@OpenApi(
    path = "/api/{schema}/query",
    methods = [HttpMethod.POST],
    summary = "Executes a query and returns the query's results.",
    operationId = "postExecuteQuery",
    tags = ["Retrieval"],
    pathParams = [
        OpenApiParam("schema", type = String::class, description = "The name of the schema to execute a query for.", required = true)
    ],
    responses = [
        OpenApiResponse("200", [OpenApiContent(QueryResult::class)]),
        OpenApiResponse("400", [OpenApiContent(ErrorStatus::class)])
    ]
)
fun executeQuery(ctx: Context, schema: Schema, runtime: RetrievalRuntime) {
    val informationNeed = try {
        ctx.bodyAsClass<InformationNeedDescription>()
    } catch (e: Exception) {
        throw ErrorStatusException(400, "Invalid request: ${e.message}")
    }
    val results = runtime.query(schema, informationNeed)
    ctx.json(QueryResult(results))
}

/**
 *
 * @author Raphael Waltenspül
 * @version 1.0

@OpenApi(
    path = "/api/{schema}/query",
    methods = [HttpMethod.POST],
    summary = "Executes a query and returns the query's results.",
    operationId = "postExecuteQuery",
    tags = ["Retrieval"],
    pathParams = [
        OpenApiParam("schema", type = String::class, description = "The name of the schema to execute a query for.", required = true),
        OpenApiParam("pipeline", type = String::class, description = "The name of the pipeline provided by the schema.", required = false)
    ],
    responses = [
        OpenApiResponse("200", [OpenApiContent(QueryResult::class)]),
        OpenApiResponse("400", [OpenApiContent(ErrorStatus::class)])
    ]
)
fun executeQuery(ctx: Context, schema: Schema, pipeline: Pipeline, runtime: RetrievalRuntime) {
    val informationNeed = try {
        ctx.bodyAsClass<PipelineInformationNeedDescription>()
    } catch (e: Exception) {
        throw ErrorStatusException(400, "Invalid request: ${e.message}")
    }
    val results = runtime.query(schema, informationNeed)
    ctx.json(QueryResult(results))
}
*/