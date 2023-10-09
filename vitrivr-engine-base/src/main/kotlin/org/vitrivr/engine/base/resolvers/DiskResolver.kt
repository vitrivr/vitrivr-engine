package org.vitrivr.engine.base.resolvers

import org.vitrivr.engine.core.model.database.retrievable.RetrievableId
import org.vitrivr.engine.core.operators.ingest.Resolvable
import org.vitrivr.engine.core.operators.ingest.Resolver
import org.vitrivr.engine.core.source.file.MimeType
import java.io.File
import java.io.OutputStream

class DiskResolver(val location: String = "./thumbnails") : Resolver {
    override fun resolve(id: RetrievableId): Resolvable? {
            val directory = File(location)
            val file = directory.listFiles { _, name -> name.startsWith(id.toString()) }?.firstOrNull()
            val mimeType = file?.let { MimeType.getMimeType(it) } ?: return null
            return Resolvable(mimeType, file.inputStream())
        }

    override fun getOutputStream(id: RetrievableId, mimeType: MimeType): OutputStream {
        val thumbnailFile = File("$location/${id}.${mimeType.fileExtension}")
        thumbnailFile.mkdirs()
        return thumbnailFile.outputStream()
    }

}