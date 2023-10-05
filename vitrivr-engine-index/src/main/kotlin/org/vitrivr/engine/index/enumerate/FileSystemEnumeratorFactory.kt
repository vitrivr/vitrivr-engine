package org.vitrivr.engine.index.enumerate

import org.bytedeco.opencv.presets.opencv_core.Str
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.operators.ingest.Enumerator
import org.vitrivr.engine.core.operators.ingest.EnumeratorFactory
import org.vitrivr.engine.core.source.MediaType
import java.time.Duration
import kotlin.io.path.Path

class FileSystemEnumeratorFactory : EnumeratorFactory {
    override fun newOperator(parameters: Map<String, Any>, schema: Schema): FileSystemEnumerator {
        val path = Path(parameters["path"] as String? ?: throw IllegalArgumentException("Path is required"))
        val depth = parameters["depth"] as Int? ?: Int.MAX_VALUE
        val mediaTypes = parameters["mediaTypes"] as Collection<MediaType>? ?: MediaType.allValid
        return FileSystemEnumerator(path, depth, mediaTypes)
    }
}