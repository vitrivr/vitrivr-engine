package org.vitrivr.engine.core.resolver.impl

import org.vitrivr.engine.core.resolver.ResolverFactory
import org.vitrivr.engine.core.model.retrievable.RetrievableId
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.source.file.MimeType
import org.vitrivr.engine.core.resolver.Resolvable
import org.vitrivr.engine.core.resolver.Resolver
import org.vitrivr.engine.core.model.descriptor.struct.metadata.source.FileSourceMetadataDescriptor
import java.io.InputStream
import java.io.OutputStream
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardOpenOption

/**
 * A custom ResolverFactory that creates an LSCFileResolver instance. This resolver is
 * specifically designed to resolve original files from the LSC dataset by looking up
 * their actual file path from the database.
 */
class LSCFileResolver : ResolverFactory {
    override fun newResolver(schema: Schema, parameters: Map<String, String>): Resolver {
        return Instance(schema)
    }

    /**
     * The actual resolver instance. It looks up the original file path from the database
     * using the FileSourceMetadataDescriptor.
     */
    private class Instance(private val schema: Schema) : Resolver {
        override fun resolve(id: RetrievableId, suffix: String): Resolvable? {

            val field = this.schema.get("file")
            if (field == null) {
                return null
            }
            val reader = field.getReader()
            // Find the descriptor for the given retrievableId.
            val descriptor = reader.getForRetrievable(id).firstOrNull()

            // If a descriptor is found use path to create a resolvable object.
            return descriptor?.let {
                val fileDescriptor = it as FileSourceMetadataDescriptor
                val originalPath = Paths.get(fileDescriptor.path.value)
                LSCFileResolvable(id, originalPath)
            }
        }

        /**
         * A Resolvable that points directly to the original file path.
         */
        inner class LSCFileResolvable(
            override val retrievableId: RetrievableId,
            private val originalPath: Path
        ) : Resolvable {
            override val mimeType: MimeType get() = MimeType.getMimeType(this.originalPath) ?: MimeType.UNKNOWN
            override fun exists(): Boolean = Files.exists(this.originalPath)
            override fun openInputStream(): InputStream = Files.newInputStream(this.originalPath, StandardOpenOption.READ)
            override fun openOutputStream(): OutputStream = throw UnsupportedOperationException("Cannot write to original files.")
        }
    }
}