package org.vitrivr.engine.index

import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.takeWhile
import kotlinx.coroutines.runBlocking
import org.vitrivr.engine.core.config.ConnectionConfig
import org.vitrivr.engine.core.config.FieldConfig
import org.vitrivr.engine.core.config.SchemaConfig
import org.vitrivr.engine.core.model.metamodel.*
import org.vitrivr.engine.index.decode.ImageDecoder
import org.vitrivr.engine.index.enumerate.FileSystemEnumerator
import org.vitrivr.engine.index.segment.PassThroughSegmenter
import java.nio.file.Paths

/**
 *
 */
fun main() {
    /* TODO. */
    playground()
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
            FieldConfig("averagecolor1", "AverageColor", ),
            FieldConfig("averagecolor2","AverageColor", )
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

        val flows = arrayOf(extractor1.toFlow(), extractor2.toFlow())

        val out = merge(*flows)

        var counter = 0

        out.takeWhile { ++counter
            !segmenter.done || (segmenter.counter * flows.size) > counter
        }.collect {
            println("${!segmenter.done} || (${segmenter.counter} * ${flows.size}) > $counter")
            println("->>$it")
        }
    }
}