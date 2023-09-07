package org.vitrivr.engine.core.api.rest.handler

import io.javalin.http.Context
import org.vitrivr.engine.core.api.rest.ErrorStatusException

interface GetRestHandler<T : Any> : RestHandler {

    fun get(ctx: Context) {
        try {
            ctx.json(doGet(ctx))
        } catch (e: ErrorStatusException) {
            ctx.status(e.statusCode)
            ctx.json(e.error)
        }
    }

    fun doGet(ctx: Context): T

}