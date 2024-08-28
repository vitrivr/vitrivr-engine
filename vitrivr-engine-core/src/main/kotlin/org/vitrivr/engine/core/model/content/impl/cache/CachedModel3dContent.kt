package org.vitrivr.engine.core.model.content.impl.cache

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.vitrivr.engine.core.model.content.element.ContentId
import org.vitrivr.engine.core.model.content.element.Model3dContent
import org.vitrivr.engine.core.model.mesh.texturemodel.Model3d
import java.lang.ref.SoftReference
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption

/**
 * A [Model3dContent] implementation that is backed by a cache file.
 *
 * This class caches a 3D model to disk in JSON format and uses a [SoftReference] to hold it in memory,
 * reloading from JSON if necessary.
 *
 * @author Rahel Arnold
 * @version 1.0.0
 */
class CachedModel3dContent(override val path: Path, model: Model3d, override val id: ContentId = ContentId.randomUUID()) : Model3dContent, CachedContent {

    /** The [SoftReference] of the [Model3d] used for caching. */
    private var reference: SoftReference<Model3d> = SoftReference(model)

    /** The [Model3d] contained in this [CachedModel3dContent]. */
    override val content: Model3d
        @Synchronized
        get() {
            var cachedModel = reference.get()
            if (cachedModel == null) {
                cachedModel = reload()
                reference = SoftReference(cachedModel)
            }
            return cachedModel
        }

    init {
        /* Serialize the Model3d to JSON and write it to the cache file during initialization. */
        Files.newBufferedWriter(this.path, StandardCharsets.UTF_8, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE).use { writer ->
            writer.write(Json.encodeToString(model))
        }
    }

    /**
     * Reloads the [Model3d] from the cached JSON file.
     *
     * @return The [Model3d] loaded from the JSON file.
     */
    private fun reload(): Model3d {
        return Files.newBufferedReader(this.path, StandardCharsets.UTF_8).use { reader ->
            Json.decodeFromString(reader.readText())
        }
    }
}