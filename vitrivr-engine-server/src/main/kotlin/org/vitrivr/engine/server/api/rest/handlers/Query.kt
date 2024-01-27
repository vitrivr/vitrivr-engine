package org.vitrivr.engine.server.api.rest.handlers

import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import io.javalin.http.Context
import io.javalin.http.bodyAsClass
import io.javalin.openapi.*
import kotlinx.serialization.json.Json
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.query.execution.RetrievalRuntime
import org.vitrivr.engine.query.model.api.InformationNeedDescription
import org.vitrivr.engine.query.model.api.result.QueryResult
import org.vitrivr.engine.server.api.rest.model.ErrorStatus
import org.vitrivr.engine.server.api.rest.model.ErrorStatusException

private val logger: KLogger = KotlinLogging.logger {}

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
    requestBody = OpenApiRequestBody([OpenApiContent(InformationNeedDescription::class)]),
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
    logger.info { "received request for ${schema.name}: ${Json.encodeToString(InformationNeedDescription.serializer(), informationNeed)}" }
    val results = runtime.query(schema, informationNeed)
    val queryResult = QueryResult(results)
    logger.info { "returning results for ${schema.name}: ${Json.encodeToString(QueryResult.serializer(), queryResult)}" }
    ctx.json(queryResult)
}