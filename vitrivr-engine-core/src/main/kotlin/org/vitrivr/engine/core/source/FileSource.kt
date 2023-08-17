package org.vitrivr.engine.core.source

import java.io.InputStream
import java.nio.file.Path

/**
 * A [Source] associated with a [Path] in the file system.
 *
 * @author Luca Rossetto
 * @author Ralph Gasser
 * @version 1.0.0
 */
@JvmRecord
data class FileSource(
    val path: Path,
    override val type: MediaType,
    override val inputStream: InputStream,
    override val name: String = path.fileName.toString(),
    override val timestamp: Long = System.currentTimeMillis(),
    override val metadata: Map<String, Any> = emptyMap()
): Source