package org.vitrivr.engine.index

import kotlinx.coroutines.flow.*
import kotlinx.coroutines.runBlocking
import org.vitrivr.engine.core.config.ConnectionConfig
import org.vitrivr.engine.core.config.FieldConfig
import org.vitrivr.engine.core.config.SchemaConfig
import org.vitrivr.engine.core.model.content.impl.InMemoryImageContent
import org.vitrivr.engine.core.model.database.retrievable.IngestedRetrievable
import org.vitrivr.engine.core.model.metamodel.*
import org.vitrivr.engine.core.operators.ingest.Segmenter
import org.vitrivr.engine.index.decode.ImageDecoder
import org.vitrivr.engine.index.enumerate.FileSystemEnumerator
import org.vitrivr.engine.index.pipeline.ExtractorLayout
import org.vitrivr.engine.index.pipeline.Pipeline
import org.vitrivr.engine.index.segment.PassThroughSegmenter
import java.nio.file.Paths

/**
 *
 */
fun main() {
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
        val enumerator = FileSystemEnumerator(Paths.get("C:\\Users\\Lucaro\\Pictures"), depth = 1)
        val decoder = ImageDecoder(enumerator)
        val segmenter = PassThroughSegmenter(decoder, this)

        val layout = ExtractorLayout(schema, listOf(listOf("averagecolor1"), listOf("averagecolor2")))

        val pipeline = Pipeline(segmenter, layout)

        pipeline.run()


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
        val segmenter = PassThroughSegmenter(decoder, this)
        val extractor1 = schema.getField(0).getExtractor(segmenter)
        val extractor2 = schema.getField(1).getExtractor(segmenter)

        terminateFlows(segmenter, extractor1.toFlow(), extractor2.toFlow()){ it, counter ->
            println("${!segmenter.inputExhausted} || ${segmenter.emitted} > $counter")
            println("->>${it.map { r -> (r.content.first() as InMemoryImageContent).source.name }}")
        }

    }
}

suspend fun <T> terminateFlows(segmenter: Segmenter, vararg flows: Flow<T>, transform: (List<T>, Int) -> Unit) {
    var counter = 0

    flows.map { flow ->
        flow.map { listOf(it) }
    }.reduceRight { f1, f2 -> f1.combine(f2) { l1, l2 -> l1 + l2 } }.takeWhile {
        ++counter
        !segmenter.inputExhausted || segmenter.emitted > counter
    }.collect {
        transform(it, counter)
    }
}