package org.vitrivr.engine.server.api.rest.handlers

import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import io.javalin.http.Context
import io.javalin.openapi.*
import io.javalin.util.FileUtil
import org.slf4j.event.LoggingEvent
import org.vitrivr.engine.core.config.pipeline.execution.ExecutionServer
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.server.api.helper.IndexThreadPool
import org.vitrivr.engine.server.api.rest.model.ErrorStatus
import org.vitrivr.engine.server.api.rest.model.ErrorStatusException
import java.nio.file.Path
import kotlin.io.path.deleteIfExists


private val logger: KLogger = KotlinLogging.logger {}

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
    val threadId = Thread.currentThread().hashCode().toString() + Thread.currentThread().id.toString()
    val basepath = Path.of("upload/$threadId/")
    try {
        ctx.uploadedFiles("data").forEach { uploadedFile ->
            val path = Path.of("$basepath/${uploadedFile.filename()}")
            FileUtil.streamToFile(uploadedFile.content(), path.toString());
            filestream.add(path)
        }
        val stream = filestream.stream()
        val pipelineBuilder = pipelineName?.let { schema.getPipelineBuilder(it) }
            ?: throw ErrorStatusException(400, "Invalid request: Pipeline '$pipelineName' does not exist.")
        // ASYNC UUID
        val pipeline = pipelineBuilder.getApiPipeline(stream)
        val id = IndexThreadPool.addThreadAndStart(
            threadId, Thread {
                try {

                    ExecutionServer().extract(pipeline)
                    logger.debug { "Thread ${Thread.currentThread().id} finished" }

                } catch (e: Exception) {
                    throw ErrorStatusException(400, "Invalid request: ${e.message}")
                }
            }
        )
        val ids = IndexThreadPool.cleanThreadPool()
        ctx.json(mapOf("id" to id, "cleaned ids" to ids))

    } catch (e: Exception) {
        throw ErrorStatusException(400, "Invalid request: ${e.message}")
    } finally {
        filestream.forEach() { file ->
            file.deleteIfExists()
        }
        basepath.deleteIfExists()
    }
}