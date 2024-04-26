package org.vitrivr.engine.core.model.content.impl.cache

import org.vitrivr.engine.core.model.content.Content
import java.nio.file.Files
import java.nio.file.Path

/**
 * [Content] as handled by vitrivr. [CachedContent] is a [Content] that is stored on disk.
 *
 * @author Luca Rossetto
 * @author Ralph Gasser
 * @version 1.0.0
 */
interface CachedContent : Content {
    /** The [Path] to the file backing this [CachedContent]. */
    val path: Path

    /**
     * Purges the content from the cache by deleting the file.
     */
    fun purge() = Files.deleteIfExists(this.path)
}