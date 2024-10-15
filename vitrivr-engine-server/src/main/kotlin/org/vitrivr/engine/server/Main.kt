package org.vitrivr.engine.server

import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import io.javalin.Javalin
import io.javalin.openapi.CookieAuth
import io.javalin.openapi.plugin.OpenApiPlugin
import io.javalin.openapi.plugin.SecurityComponentConfiguration
import io.javalin.openapi.plugin.swagger.SwaggerPlugin
import org.vitrivr.engine.core.config.pipeline.execution.ExecutionServer
import org.vitrivr.engine.core.model.metamodel.SchemaManager
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
    for ((name, schemaConfig) in config.schemas) {
        manager.load(name, schemaConfig)
    }

    /* Execution server singleton for this instance. */
    val executor = ExecutionServer()

    /* Prepare Javalin endpoint. */
    val javalin = Javalin.create { c ->
        /* Apply Kotlinx JSON mapper. */
        c.jsonMapper(KotlinxJsonMapper)

        /* Registers Open API plugin. */
        c.registerPlugin(OpenApiPlugin {
            it.withDocumentationPath("/openapi.json")
                .withDefinitionConfiguration { _, def ->
                    def.withInfo { i ->
                        i.title = "vitrivr engine API"
                        i.version = "0.1.0"
                        i.description =
                            "Rest API for the vitrivr engine project. Provides query (runtime) and extraction (ingestion) endpoints"
                    }
                        .withSecurity(
                            SecurityComponentConfiguration().withSecurityScheme("CookieAuth", CookieAuth("SESSIONID"))
                        )
                }
        })
        c.http.maxRequestSize = 1024 * 1024 * 1024 /* 1GB */

        c.bundledPlugins.enableCors { cors ->
            /* https://javalin.io/plugins/cors#getting-started */
            cors.addRule {
                it.reflectClientOrigin = true // might be a little too loose
                it.allowCredentials = true
            }
        }


        /* Registers Swagger Plugin. */
        c.registerPlugin(SwaggerPlugin { swaggerConfig ->
            swaggerConfig.documentationPath = "/openapi.json"
            swaggerConfig.uiPath = "/swagger-ui"
        })

        c.router.apiBuilder {
            configureApiRoutes(config.api, manager, executor)
        }
    }.exception(ErrorStatusException::class.java) { e, ctx ->
        ctx.status(e.statusCode).json(ErrorStatus(e.message))
    }.exception(Exception::class.java) { e, ctx ->
        ctx.status(500).json(ErrorStatus("Internal Server Error: '${e.message}' @ ${e.stackTrace.first()}"))
    }

    /* Prepare CLI endpoint. */
    val cli = Cli(manager)
    for (schema in manager.listSchemas()) {
        cli.register(SchemaCommand(schema, executor))
    }

    /* Start the Javalin server. */
    javalin.start(config.api.port)
    logger.info { "vitrivr engine API is listening on port ${config.api.port}." }

    /* Start the CLI in a new thread;  this will block the thread it runs on. */
    cli.start()

    /* Upon reaching this point, the program was aborted. */
    /* Stop the Javalin server. */
    javalin.stop()
}
