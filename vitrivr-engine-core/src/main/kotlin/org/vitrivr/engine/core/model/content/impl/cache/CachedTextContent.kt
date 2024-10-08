package org.vitrivr.engine.core.model.content.impl.cache

import org.vitrivr.engine.core.model.content.element.ContentId
import org.vitrivr.engine.core.model.content.element.TextContent
import java.lang.ref.SoftReference
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption

/**
 * A [TextContent] implementation that is backed by a cache file.
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
class CachedTextContent(override val path: Path, text: String, override val id: ContentId = ContentId.randomUUID()) : TextContent, CachedContent {

    /** The [SoftReference] of the [String] used for caching. */
    private var reference: SoftReference<String> = SoftReference(text)

    /** The length of the text is stored explicitly.  */
    override val length: Int = text.length

    /** The [String] contained in this [CachedTextContent]. */
    override val content: String
        @Synchronized
        get() {
            var image = this.reference.get()
            if (image == null) {
                image = reload()
                this.reference = SoftReference(image)
            }
            return image
        }

    init {
        /* Writes text to disk. */
        Files.newBufferedWriter(this.path, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE).use {
            it.write(text)
        }
    }

    /**
     * Reloads the text from disk.
     *
     * @return [String]
     */
    private fun reload(): String = Files.newBufferedReader(this.path).use {
        it.readText()
    }
}