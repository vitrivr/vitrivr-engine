package org.vitrivr.engine.index

import kotlinx.coroutines.flow.collect
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
        val enumerator = FileSystemEnumerator(Paths.get("C:\\Users\\Lucaro\\Pictures"))
        val decoder = ImageDecoder(enumerator)
        val segmenter = PassThroughSegmenter(decoder, this)
        val extractor1 = schema.fields[0].getExtractor(segmenter)
        val extractor2 = schema.fields[1].getExtractor(extractor1)
        extractor2.toFlow().collect {
            println(it)
        }
    }
}