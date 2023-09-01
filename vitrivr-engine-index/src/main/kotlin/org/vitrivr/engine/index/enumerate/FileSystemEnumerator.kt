package org.vitrivr.engine.index.enumerate

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import org.vitrivr.engine.core.operators.ingest.Enumerator
import org.vitrivr.engine.core.source.file.FileSource
import org.vitrivr.engine.core.source.MediaType
import org.vitrivr.engine.core.source.file.MimeType
import org.vitrivr.engine.core.source.Source
import java.io.IOException
import java.nio.file.FileVisitOption
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import kotlin.io.path.isRegularFile

/**
 * An [Enumerator] that enumerates a [Path] in a file system.
 *
 * @author Luca Rossetto
 * @author Ralph Gasser
 * @version 1.0.0
 */
class FileSystemEnumerator(private val path: Path, private val depth: Int = Int.MAX_VALUE, private val mediaTypes: Collection<MediaType> = MediaType.allValid) : Enumerator {
    override fun toFlow(scope: CoroutineScope): Flow<Source> = flow {
        val stream = Files.walk(this@FileSystemEnumerator.path, this@FileSystemEnumerator.depth, FileVisitOption.FOLLOW_LINKS).filter { it.isRegularFile() }
        for (element in stream) {
            try {
                val type = MimeType.getMimeType(element) ?: continue
                if (type.mediaType in this@FileSystemEnumerator.mediaTypes) {
                    emit(FileSource(element, type, Files.newInputStream(element, StandardOpenOption.READ)))
                }
            } catch (e: IOException) {
                // TODO log
            }
        }
    }.flowOn(Dispatchers.IO)
}