package org.vitrivr.engine.core.api.rest.handler

import io.javalin.http.Context
import org.vitrivr.engine.core.api.rest.ErrorStatusException

interface DeleteRestHandler<T: Any> : RestHandler {

    fun delete(ctx: Context) {
        try {
            ctx.json(doDelete(ctx))
        } catch (e: ErrorStatusException) {
            ctx.status(e.statusCode)
            ctx.json(e.error)
        }
    }

    fun doDelete(ctx: Context): T

}