package org.vitrivr.engine.server.api.rest.handlers

import io.javalin.http.Context
import io.javalin.openapi.*
import io.javalin.util.FileUtil
import org.vitrivr.engine.core.config.pipeline.execution.ExecutionServer
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.server.api.rest.model.ErrorStatus
import org.vitrivr.engine.server.api.rest.model.ErrorStatusException
import org.vitrivr.engine.server.api.rest.model.IngestStatus
import java.nio.file.Path
import java.util.*
import kotlin.io.path.deleteIfExists

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
        OpenApiResponse("200", [OpenApiContent(IngestStatus::class)]),
        OpenApiResponse("400", [OpenApiContent(ErrorStatus::class)])
    ]
)
fun executeIngest(ctx: Context, schema: Schema, executor: ExecutionServer) {
    val pipelineName = try {
        ctx.formParam("pipelineName")
    } catch (e: Exception) {
        throw ErrorStatusException(400, "Invalid request: ${e.message}")
    }
    val filestream: MutableList<Path> = mutableListOf()
    // folder with threadId to avoid deleting files from other threads
    val uuid = UUID.randomUUID()
    val basepath = Path.of("upload/$uuid/")
    try {
        /* Handle uploaded file. */
        ctx.uploadedFiles("data").forEach { uploadedFile ->
            val path = Path.of("$basepath/${uploadedFile.filename()}")
            FileUtil.streamToFile(uploadedFile.content(), path.toString())
            filestream.add(path)
        }
        val stream = filestream.stream()

        /* Construct extraction pipeline */
        val pipelineBuilder = pipelineName?.let { schema.getIngestionPipelineBuilder(it) }
            ?: throw ErrorStatusException(404, "Invalid request: Pipeline '$pipelineName' does not exist.")
        val pipeline = pipelineBuilder.build(stream)

        /* Schedule pipeline and return job Id. */
        val jobId = executor.extractAsync(pipeline.first())
        ctx.json(IngestStatus(jobId.toString(), executor.status(jobId), System.currentTimeMillis()))
    } catch (e: Exception) {
        throw ErrorStatusException(400, "Invalid request: ${e.message}")
    } finally {
        filestream.forEach { file -> file.deleteIfExists() }
        basepath.deleteIfExists()
    }
}

@OpenApi(
    path = "/api/{schema}/index/{jobId}",
    methods = [HttpMethod.GET],
    summary = "Queries the status of a given ingest job.",
    operationId = "getIngestStatus",
    tags = ["Ingest"],
    pathParams = [
        OpenApiParam("schema", type = String::class, description = "The name of the schema to execute a query for.", required = true),
        OpenApiParam("jobId", type = String::class, description = "The id of the job to query the status for.", required = true)
    ],
    responses = [
        OpenApiResponse("200", [OpenApiContent(IngestStatus::class)]),
        OpenApiResponse("400", [OpenApiContent(ErrorStatus::class)])
    ]
)
fun executeIngestStatus(ctx: Context, executor: ExecutionServer) {
    val id = try {
        UUID.fromString(ctx.pathParam("jobId"))
    } catch (e: Exception) {
        throw ErrorStatusException(400, "Invalid request: ${e.message}")
    }
    ctx.json(IngestStatus(id.toString(), executor.status(id), System.currentTimeMillis()))
}
