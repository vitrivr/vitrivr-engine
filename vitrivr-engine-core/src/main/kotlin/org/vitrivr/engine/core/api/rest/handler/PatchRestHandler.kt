package org.vitrivr.engine.core.api.rest.handler

import io.javalin.http.Context
import org.vitrivr.engine.core.api.rest.ErrorStatusException

interface PatchRestHandler<T: Any> : RestHandler {

    fun patch(ctx: Context) {
        try {
            ctx.json(doPatch(ctx))
        } catch (e: ErrorStatusException) {
            ctx.status(e.statusCode)
            ctx.json(e.error)
        }
    }

    fun doPatch(ctx: Context): T

}