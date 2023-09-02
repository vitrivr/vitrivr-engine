package org.vitrivr.engine.query

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.internal.readJson
import org.vitrivr.engine.core.config.ConnectionConfig
import org.vitrivr.engine.core.config.FieldConfig
import org.vitrivr.engine.core.config.SchemaConfig
import org.vitrivr.engine.core.config.VitrivrConfig
import org.vitrivr.engine.core.model.metamodel.SchemaManager
import org.vitrivr.engine.query.execution.RetrievalRuntime
import org.vitrivr.engine.query.model.api.InformationNeedDescription
import java.nio.file.Paths
import kotlin.system.exitProcess

/**
 * Entry point for vitrivr engine query.
 */
fun main(args: Array<String>) {

    informationNeed()

}


fun informationNeed() {

    val inputString = """
        {
            "inputs": {
                "mytext": {"type": "TEXT", "data": "SOME TEXT"},
                "my other text": {"type": "TEXT", "data": "some other text"},
                "vec": {"type": "VECTOR", "data": [1.0, 0.0, 1.0]}
            },
            "operations": {
                "averagecolor" : {"type": "RETRIEVER", "input": "vec"}
            },
            "output": "averagecolor"
        }
    """.trimIndent()

    val description = Json.decodeFromString<InformationNeedDescription>(inputString)


    val schemaConfig = SchemaConfig(
        "vitrivr",
        ConnectionConfig("String", mapOf("host" to "127.0.0.1", "port" to "1865")),
        listOf(
            FieldConfig("averagecolor", "AverageColor"),
        )
    )

    println(description)

    val schema = SchemaManager.open(schemaConfig)
    schema.initialize()

    val runtime = RetrievalRuntime()

    val results = runtime.query(schema, description)

    println(results)

}

fun vitrivrConfig(args: Array<String>) {
    /* Load system configuration. */
    val config = VitrivrConfig.read(Paths.get(args.getOrElse(0) { VitrivrConfig.DEFAULT_SCHEMA_PATH })) ?: exitProcess(1)

    /* Open all schemas. */
    for (schema in config.schemas) {
        SchemaManager.open(schema)
    }
}