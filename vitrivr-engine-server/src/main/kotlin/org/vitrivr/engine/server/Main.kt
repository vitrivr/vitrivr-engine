package org.vitrivr.engine.server

import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import io.javalin.Javalin
import io.javalin.openapi.CookieAuth
import io.javalin.openapi.plugin.OpenApiPlugin
import io.javalin.openapi.plugin.OpenApiPluginConfiguration
import io.javalin.openapi.plugin.SecurityComponentConfiguration
import io.javalin.openapi.plugin.swagger.SwaggerConfiguration
import io.javalin.openapi.plugin.swagger.SwaggerPlugin
import org.vitrivr.engine.core.model.metamodel.SchemaManager
import org.vitrivr.engine.core.config.pipeline.execution.ExecutionServer
import org.vitrivr.engine.query.execution.RetrievalRuntime
import org.vitrivr.engine.server.api.cli.Cli
import org.vitrivr.engine.server.api.cli.commands.SchemaCommand
import org.vitrivr.engine.server.api.rest.KotlinxJsonMapper
import org.vitrivr.engine.server.api.rest.configureApiRoutes
import org.vitrivr.engine.server.api.rest.model.ErrorStatus
import org.vitrivr.engine.server.api.rest.model.ErrorStatusException
import org.vitrivr.engine.server.config.ServerConfig
import org.vitrivr.engine.server.config.ServerConfig.Companion.DEFAULT_SCHEMA_PATH
import java.nio.file.Paths
import kotlin.system.exitProcess

private val logger: KLogger = KotlinLogging.logger {}

/**
 * Entry point for vitrivr engine (index + query).
 */
fun main(args: Array<String>) {
    /* Load system configuration. */
    val config = ServerConfig.read(Paths.get(args.getOrElse(0) { DEFAULT_SCHEMA_PATH })) ?: exitProcess(1)

    /* Setup schema manager. */
    val manager = SchemaManager()
    for (schema in config.schemas) {
        manager.load(schema)
    }

    /* Initialize retrieval runtime. */
    val runtime = RetrievalRuntime()

    /* Prepare Javalin endpoint. */
    val javalin = Javalin.create { c ->
        c.jsonMapper(KotlinxJsonMapper)

        /* Registers Open API plugin. */
        c.plugins.register(
            OpenApiPlugin(
                OpenApiPluginConfiguration()
                    .withDocumentationPath("/swagger-docs")
                    .withDefinitionConfiguration { _, u ->
                        u.withOpenApiInfo { t ->
                            t.title = "vitrivr engine API"
                            t.version = "1.0.0"
                            t.description = "API for the vitrivr engine."
                        }
                        u.withSecurity(
                            SecurityComponentConfiguration().withSecurityScheme("CookieAuth", CookieAuth("SESSIONID"))
                        )
                    }
            )
        )
        c.http.maxRequestSize = 1024 * 1024 * 1024 /* 1GB */

        /* Registers Swagger Plugin. */
        c.plugins.register(
            SwaggerPlugin(
                SwaggerConfiguration().apply {
                    this.documentationPath = "/swagger-docs"
                    this.uiPath = "/swagger-ui"
                }
            )
        )
    }.routes {
        configureApiRoutes(config.api, manager, runtime)
    }.exception(ErrorStatusException::class.java) { e, ctx ->
        ctx.status(e.statusCode).json(ErrorStatus(e.message))
    }.exception(Exception::class.java) { e, ctx ->
        ctx.status(500).json(ErrorStatus("Internal Server Error: '${e.message}' @ ${e.stackTrace.first()}"))
    }

    /* Prepare CLI endpoint. */
    val cli = Cli(manager)
    for (schema in manager.listSchemas()) {
        cli.register(SchemaCommand(schema, schema.getExecutionServer()))
    }

    /* Start the Javalin and CLI. */
    javalin.start(config.api.port)
    logger.info { "vitrivr engine API is listening on port ${config.api.port}." }
    cli.start() /* Blocks. */

    /* End Javalin once Cli is stopped. */
    javalin.stop()
}