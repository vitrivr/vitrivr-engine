package org.vitrivr.engine.server.api.rest.handlers

import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import io.javalin.http.Context
import io.javalin.http.bodyAsClass
import io.javalin.openapi.*
import org.vitrivr.engine.core.config.pipeline.execution.ExecutionServer
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.query.model.api.InformationNeedDescription
import org.vitrivr.engine.query.model.api.result.QueryResult
import org.vitrivr.engine.query.parsing.QueryParser
import org.vitrivr.engine.server.api.rest.model.ErrorStatus
import org.vitrivr.engine.server.api.rest.model.ErrorStatusException
import kotlin.time.measureTime

private val logger: KLogger = KotlinLogging.logger {}

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
fun executeQuery(ctx: Context, schema: Schema, executor: ExecutionServer) {
    val duration = measureTime {
        /* Extract information need. */
        val informationNeed = try {
            ctx.bodyAsClass<InformationNeedDescription>()
        } catch (e: Exception) {
            throw ErrorStatusException(400, "Invalid request: ${e.message}")
        }

        /* Obtain query parser. */
        val operator = QueryParser(schema).parse(informationNeed)

        /* Execute query. */
        val results = executor.query(operator)
        ctx.json(QueryResult(results))
    }
    logger.info { "Executing ${ctx.req().pathInfo} took $duration." }
}