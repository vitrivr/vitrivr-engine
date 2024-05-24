package org.vitrivr.engine.core.source.file

import org.vitrivr.engine.core.source.MediaType
import org.vitrivr.engine.core.source.Source
import org.vitrivr.engine.core.source.SourceId
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import java.util.*

/**
 * A [Source] associated with a [Path] in the file system.
 *
 * @author Luca Rossetto
 * @author Ralph Gasser
 * @version 1.1.0
 */
data class FileSource(
    override val sourceId: SourceId = UUID.randomUUID(),
    val path: Path,
    val mimeType: MimeType,
    override val name: String = path.fileName.toString(),
    override val timestamp: Long = System.currentTimeMillis(),
    override val metadata: MutableMap<String, Any> = mutableMapOf()
): Source {
    override val type: MediaType
        get() = this.mimeType.mediaType

    /**
     * Opens a [InputStream] for this [FileSource]. It remains up to the caller to open the [InputStream].
     *
     * @return [InputStream] for this [FileSource]
     */
    override fun newInputStream(): InputStream = Files.newInputStream(this.path, StandardOpenOption.READ)

    /**
     * String representation of this [FileSource].
     */
    override fun toString() = "FileSource(name = $name, mime = $mimeType, type = $type, path = $path)"
}