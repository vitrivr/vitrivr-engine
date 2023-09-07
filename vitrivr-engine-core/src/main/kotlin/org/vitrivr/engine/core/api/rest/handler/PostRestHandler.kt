package org.vitrivr.engine.core.api.rest.handler

import io.javalin.http.Context
import org.vitrivr.engine.core.api.rest.ErrorStatusException

interface PostRestHandler<T : Any> : RestHandler {

    fun post(ctx: Context) {
        try {
            ctx.json(doPost(ctx))
        } catch (e: ErrorStatusException) {
            ctx.status(e.statusCode)
            ctx.json(e.error)
        }
    }

    fun doPost(ctx: Context): T

}