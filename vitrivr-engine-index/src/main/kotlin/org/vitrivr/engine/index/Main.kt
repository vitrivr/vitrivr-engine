package org.vitrivr.engine.index

import org.vitrivr.engine.core.api.cli.Cli
import org.vitrivr.engine.core.config.ConnectionConfig
import org.vitrivr.engine.core.config.FieldConfig
import org.vitrivr.engine.core.config.SchemaConfig
import org.vitrivr.engine.core.config.VitrivrConfig
import org.vitrivr.engine.core.config.VitrivrConfig.Companion.DEFAULT_SCHEMA_PATH
import org.vitrivr.engine.core.model.metamodel.*
import org.vitrivr.engine.index.decode.ImageDecoder
import org.vitrivr.engine.index.enumerate.FileSystemEnumerator
import org.vitrivr.engine.index.execution.ExecutionServer
import org.vitrivr.engine.index.segment.PassThroughSegmenter
import java.nio.file.Paths
import kotlin.system.exitProcess

/**
 * Entry point for vitrivr engine index.
 */
fun main(args: Array<String>) {
    /* Load system configuration. */
    val config = VitrivrConfig.read(Paths.get(args.getOrElse(0) { DEFAULT_SCHEMA_PATH })) ?: exitProcess(1)

    /* Open all schemas. */
    for (schema in config.schemas) {
        SchemaManager.open(schema)
    }

    /* The execution server used by vitrivr-engine index. */
    val server = ExecutionServer()

    /* Start the CLI. */
    val cli = Cli()
    cli.start()
}

/**
 *
 */
fun playground(server: ExecutionServer) {
    /* Prepare schema. */
    val schemaConfig = SchemaConfig(
        "vitrivr",
        ConnectionConfig("String", mapOf("host" to "127.0.0.1", "port" to "1865")),
        listOf(
            FieldConfig("averagecolor1", "AverageColor"),
            FieldConfig("averagecolor2", "AverageColor")
        )
    )

    val schema = SchemaManager.open(schemaConfig)
    schema.initialize()

    /* Create pipeline and process it. */
    val enumerator = FileSystemEnumerator(Paths.get("/Users/rgasser/Downloads/dres/collection"), depth = 1)
    val decoder = ImageDecoder(enumerator)
    val segmenter = PassThroughSegmenter(decoder, schema.connection.getRetrievableWriter())
    val extractor1 = schema.getField(0).getExtractor(segmenter)
    val extractor2 = schema.getField(1).getExtractor(segmenter)

    server.extract(listOf(extractor1, extractor2))

    println("done")

    server.shutdown()


}

