package org.vitrivr.engine.server.api.rest.handlers

import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import io.javalin.http.Context
import io.javalin.openapi.*
import io.javalin.util.FileUtil
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.server.api.rest.model.ErrorStatus
import org.vitrivr.engine.server.api.rest.model.ErrorStatusException
import java.nio.file.Path
import java.util.*
import kotlin.io.path.deleteIfExists


private val logger: KLogger = KotlinLogging.logger {}

/**
 *
 * @author Ralph Gasser
 * @author Raphael
 * @version 1.0
 */
@OpenApi(
    path = "/api/{schema}/index",
    methods = [HttpMethod.POST],
    summary = "Indexes an item, adding it to the defined schema.",
    operationId = "postExecuteIngest",
    tags = ["Ingest"],
    pathParams = [
        OpenApiParam(
            "schema",
            type = String::class,
            description = "The name of the schema to execute a query for.",
            required = true
        )
    ],
    responses = [
        OpenApiResponse("200", [OpenApiContent(Any::class)]),
        OpenApiResponse("400", [OpenApiContent(ErrorStatus::class)])
    ]
)
fun executeIngest(ctx: Context, schema: Schema) {
    val pipelineName = try {
        ctx.formParam("pipelineName")
    } catch (e: Exception) {
        throw ErrorStatusException(400, "Invalid request: ${e.message}")
    }
    val filestream: MutableList<Path> = mutableListOf()
    // folder with threadId to avoid deleting files from other threads
    val uuid = UUID.randomUUID();
    val basepath = Path.of("upload/$uuid/")

    try {
        ctx.uploadedFiles("data").forEach { uploadedFile ->
            val path = Path.of("$basepath/${uploadedFile.filename()}")
            FileUtil.streamToFile(uploadedFile.content(), path.toString());
            filestream.add(path)
        }
        val stream = filestream.stream()
        val pipelineBuilder = pipelineName?.let { schema.getPipelineBuilder(it) }
            ?: throw ErrorStatusException(400, "Invalid request: Pipeline '$pipelineName' does not exist.")
        val pipeline = pipelineBuilder.getApiPipeline(stream)

        schema.getExecutionServer().enqueueIndexJob(pipeline, uuid)
        ctx.json(mapOf("id" to uuid))

    } catch (e: Exception) {
        throw ErrorStatusException(400, "Invalid request: ${e.message}")
    } finally {
        filestream.forEach() { file ->
            file.deleteIfExists()
        }
        basepath.deleteIfExists()
    }
}

/**
 *
 * @author Raphael
 * @version 1.0
 */
@OpenApi(
    path = "/api/{schema}/index/{id}",
    methods = [HttpMethod.GET],
    summary = "Indexes an item, adding it to the defined schema.",
    operationId = "postExecuteIngest",
    tags = ["Ingest"],
    pathParams = [
        OpenApiParam(
            "schema",
            type = String::class,
            description = "The name of the schema to execute a query for.",
            required = true
        ), OpenApiParam(
            "id",
            type = String::class,
            description = "The id querying the state.",
            required = true
        )
    ],
    responses = [
        OpenApiResponse("200", [OpenApiContent(Any::class)]),
        OpenApiResponse("400", [OpenApiContent(ErrorStatus::class)])
    ]
)
fun executeIngestStatus(ctx: Context, schema: Schema) {
    val id = try {
        UUID.fromString(ctx.pathParam("id"))
    } catch (e: Exception) {
        throw ErrorStatusException(400, "Invalid request: ${e.message}")
    }
    val status = schema.getExecutionServer().isPending(id)
    ctx.json(mapOf("status" to status))
}