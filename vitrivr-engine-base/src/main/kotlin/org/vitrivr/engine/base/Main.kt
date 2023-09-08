package org.vitrivr.engine.base

import org.vitrivr.engine.core.api.rest.javalin.addHandlers
import org.vitrivr.engine.core.api.rest.javalin.javalinDefaultSetup
import org.vitrivr.engine.core.config.ConnectionConfig
import org.vitrivr.engine.core.config.FieldConfig
import org.vitrivr.engine.core.config.SchemaConfig
import org.vitrivr.engine.core.model.metamodel.SchemaManager
import org.vitrivr.engine.query.api.handler.QueryPostHandler
import org.vitrivr.engine.query.execution.RetrievalRuntime

object Main {

    @JvmStatic
    fun main(args: Array<String>) {

        val schemaConfig = SchemaConfig(
            "vitrivr",
            ConnectionConfig("String", mapOf("host" to "127.0.0.1", "port" to "1865")),
            listOf(
                FieldConfig("averagecolor", "AverageColor"),
            )
        )

        val schema = SchemaManager.open(schemaConfig)

        val runtime = RetrievalRuntime()

        val javalin = javalinDefaultSetup().addHandlers(
            QueryPostHandler(schema, runtime)
        ).start(8080)


        do {
            println("type 'quit' to exit")
        } while (readln() != "quit")

        javalin.stop()

    }

}