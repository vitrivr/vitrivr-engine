package org.vitrivr.engine.query.api.handler

import io.javalin.http.Context
import io.javalin.http.bodyAsClass
import org.vitrivr.engine.core.api.rest.ErrorStatusException
import org.vitrivr.engine.core.api.rest.handler.PostRestHandler
import org.vitrivr.engine.core.model.database.retrievable.ScoredRetrievable
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.query.execution.RetrievalRuntime
import org.vitrivr.engine.query.model.api.InformationNeedDescription
import org.vitrivr.engine.query.model.api.result.QueryResult

class QueryPostHandler(private val schema: Schema, private val runtime: RetrievalRuntime) : PostRestHandler<QueryResult> {

    override val route = "query"
    override val apiVersion = "v1"


    override fun doPost(ctx: Context): QueryResult {

        val informationNeed = try {
            ctx.bodyAsClass<InformationNeedDescription>()
        } catch (e: Exception) {
            throw ErrorStatusException(400, "Invalid request: ${e.message}")
        }

        val results = runtime.query(schema, informationNeed)

        return QueryResult(results)
    }


}