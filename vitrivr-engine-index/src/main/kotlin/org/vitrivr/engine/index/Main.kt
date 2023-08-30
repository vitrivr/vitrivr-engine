package org.vitrivr.engine.index

import kotlinx.coroutines.runBlocking
import org.vitrivr.engine.core.config.ConnectionConfig
import org.vitrivr.engine.core.config.FieldConfig
import org.vitrivr.engine.core.config.SchemaConfig
import org.vitrivr.engine.core.config.VitrivrConfig
import org.vitrivr.engine.core.config.VitrivrConfig.Companion.DEFAULT_SCHEMA_PATH
import org.vitrivr.engine.core.model.content.impl.InMemoryImageContent
import org.vitrivr.engine.core.model.metamodel.*
import org.vitrivr.engine.core.util.extension.terminateFlows
import org.vitrivr.engine.index.decode.ImageDecoder
import org.vitrivr.engine.index.enumerate.FileSystemEnumerator
import org.vitrivr.engine.index.pipeline.ExtractorLayout
import org.vitrivr.engine.index.pipeline.Pipeline
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

    /* TODO. */
    //playground()
    pipelineTest()
}

fun pipelineTest() {





    val schemaConfig = SchemaConfig(
        "vitrivr",
        ConnectionConfig("String", mapOf()),
        listOf(
            FieldConfig("averagecolor1", "AverageColor"),
            FieldConfig("averagecolor2", "AverageColor")
        )
    )

    val schema = SchemaManager.open(schemaConfig)
    schema.initialize()


    /* Create pipeline and process it. */
    runBlocking {
        val enumerator = FileSystemEnumerator(Paths.get("C:\\Users\\Lucaro\\Pictures\\test"), depth = 1)
        val decoder = ImageDecoder(enumerator)
        val segmenter = PassThroughSegmenter(decoder, this, schema.connection.getRetrievableWriter())

        val layout = ExtractorLayout(
            schema,
            listOf(
                listOf(ExtractorLayout.ExtractorOption("averagecolor1")),
                listOf(ExtractorLayout.ExtractorOption("averagecolor2"))
            )
        )

        val pipeline = Pipeline(segmenter, layout)

        pipeline.run { list, counter ->
            println("-$counter->>${list.map { r -> (r.content.first() as InMemoryImageContent).source.name }}")
        }


    }

}

/**
 *
 */
fun playground() {
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
    runBlocking {
        val enumerator = FileSystemEnumerator(Paths.get("C:\\Users\\Lucaro\\Pictures"), depth = 1)
        val decoder = ImageDecoder(enumerator)
        val segmenter = PassThroughSegmenter(decoder, this, schema.connection.getRetrievableWriter())
        val extractor1 = schema.getField(0).getExtractor(segmenter)
        val extractor2 = schema.getField(1).getExtractor(segmenter)

        segmenter.terminateFlows(extractor1.toFlow(), extractor2.toFlow()) { it, counter ->
            println("${!segmenter.inputExhausted} || ${segmenter.emitted} > $counter")
            println("->>${it.map { r -> (r.content.first() as InMemoryImageContent).source.name }}")
        }

    }
}

