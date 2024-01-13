package org.vitrivr.engine.server.api.rest

import io.javalin.apibuilder.ApiBuilder.*
import org.vitrivr.engine.core.config.pipeline.execution.ExecutionServer
import org.vitrivr.engine.core.model.metamodel.SchemaManager
import org.vitrivr.engine.query.execution.RetrievalRuntime
import org.vitrivr.engine.server.api.rest.handlers.*
import org.vitrivr.engine.server.config.ApiConfig


/**
 * Configures all the API routes.
 *
 * @param config The [ApiConfig] used for persistence.
 * @param manager The [SchemaManager] used for persistence.
 */
fun configureApiRoutes(config: ApiConfig, manager: SchemaManager, retrievalRuntime: RetrievalRuntime, executor: ExecutionServer) {
    path("api") {
        /* Add global routes (non-schema specific). */
        path("schema") {
            get("list") { ctx -> listSchemas(ctx, manager) }
        }

        /* Add global routes (module specific). */
        if (config.retrieval) {
            /* TODO: Retrieval-only, global routes. */
        }
        if (config.index) {
            /* TODO: Index-only, global routes. */
        }

        /* Add schema specific routes. */
        for (schema in manager.listSchemas()) {
            path(schema.name) {
                if (config.index) {
                    post("index") { ctx -> executeIngest(ctx, schema, executor) }
                    path("index") {
                        get("{id}") { ctx -> executeIngestStatus(ctx, executor) }
                    }
                }

                if (config.retrieval) {
                    post("query") { ctx -> executeQuery(ctx, schema, retrievalRuntime) }
                }

                if (config.export) {

                    path("fetch") {
                        path("{exporter}") {
                            path("{retrievable}") {
                                post { ctx ->
                                    fetchExportData(ctx, schema)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}