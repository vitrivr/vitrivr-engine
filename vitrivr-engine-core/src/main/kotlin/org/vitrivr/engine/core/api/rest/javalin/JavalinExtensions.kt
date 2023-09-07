package org.vitrivr.engine.core.api.rest.javalin

import io.javalin.Javalin
import io.javalin.apibuilder.ApiBuilder
import io.javalin.apibuilder.ApiBuilder.path
import org.vitrivr.engine.core.api.rest.handler.*

fun Javalin.addHandlers(vararg handlers: RestHandler): Javalin =
    this.routes {
        path("api") {
            handlers.groupBy { it.apiVersion }.forEach { apiGroup ->
                path(apiGroup.key) {
                    apiGroup.value.forEach { handler ->
                        path(handler.route) {

                            if (handler is GetRestHandler<*>) {
                                ApiBuilder.get(handler::get)
                            }

                            if (handler is PostRestHandler<*>) {
                                ApiBuilder.post(handler::post)
                            }

                            if (handler is PatchRestHandler<*>) {
                                ApiBuilder.patch(handler::patch)
                            }

                            if (handler is DeleteRestHandler<*>) {
                                ApiBuilder.delete(handler::delete)
                            }

                        }
                    }
                }
            }
        }
    }
