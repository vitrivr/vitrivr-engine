package org.vitrivr.engine.enumerate

import org.vitrivr.engine.core.operators.ingest.Enumerator
import org.vitrivr.engine.core.source.FileSource
import org.vitrivr.engine.core.source.MediaType
import org.vitrivr.engine.core.source.MimeType
import org.vitrivr.engine.core.source.Source
import java.io.File
import java.io.FileInputStream

class FileSystemEnumerator(
    start: File,
    depth: Int = Int.MAX_VALUE,
    private val mediaTypes: Set<MediaType> = setOf(
        MediaType.IMAGE,
        MediaType.AUDIO,
        MediaType.VIDEO,
        MediaType.MESH
    )
) : Enumerator {

    private val iterator = start.walkTopDown().maxDepth(depth).asSequence().filter { file ->
        if (file.isDirectory) {
            return@filter false
        }
        val mimeType = MimeType.getMimeType(file)
        mimeType != null && mediaTypes.contains(mimeType.mediaType)
    }.map {
        FileSource(
            it.toPath(), MimeType.getMimeType(it)!!.mediaType, FileInputStream(it)
        )
    }.iterator()


    override fun hasNext(): Boolean = this.iterator.hasNext()

    override fun next(): Source = this.iterator.next()
}