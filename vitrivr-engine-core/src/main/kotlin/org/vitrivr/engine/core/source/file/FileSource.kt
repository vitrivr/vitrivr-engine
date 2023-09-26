package org.vitrivr.engine.core.source.file

import org.vitrivr.engine.core.source.MediaType
import org.vitrivr.engine.core.source.Source
import java.io.InputStream
import java.nio.file.Path

/**
 * A [Source] associated with a [Path] in the file system.
 *
 * @author Luca Rossetto
 * @author Ralph Gasser
 * @version 1.0.0
 */

data class FileSource(
    val path: Path,
    val mimeType: MimeType,
    override val inputStream: InputStream,
    override val name: String = path.fileName.toString(),
    override val timestamp: Long = System.currentTimeMillis(),
    override val metadata: Map<String, Any> = emptyMap()
): Source {
    override val type: MediaType
        get() = this.mimeType.mediaType

    /**
     * String representation of this [FileSource].
     */
    override fun toString() = "FileSource(name = $name, mime = $mimeType, type = $type, path = $path)"
}