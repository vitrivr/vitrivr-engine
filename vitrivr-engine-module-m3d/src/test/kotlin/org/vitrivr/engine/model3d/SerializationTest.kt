package org.vitrivr.engine.model3d

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.vitrivr.engine.core.model.content.impl.cache.CachedModel3dContent
import org.vitrivr.engine.core.model.mesh.texturemodel.Material
import org.vitrivr.engine.core.model.mesh.texturemodel.Model3d
import java.io.File
import kotlin.io.path.Path

/**
 * Unit tests to check the serialization of 3D models.
 *
 * @author Rahel Arnold
 * @version 1.0.0
 */
class SerializationTest {
    /*
        * Tests if a 3D model is identical to after serialization and deserialization.
     */
    @Test
    fun serialization() {
        val loader = ModelLoader()
        val model = this::class.java.getResourceAsStream("/bunny.obj").use { inp ->
            loader.loadModel("bunny", inp!!)
        } ?: Assertions.fail("Failed to load model.")

        /* Serialize and deserialize model. */
        val serialized = Json.encodeToString(model)
        val deserialized = Json.decodeFromString<Model3d>(serialized)

        /* Check if original and deserialized models are the same. */
        Assertions.assertEquals(model, deserialized)
    }

    /**
     * Tests if a 3D model is identical to reloaded one from cache.
     */
    @Test
    fun cachedModel() {
        val tmpPath = Path("./model.json")/* Load model. */
        val loader = ModelLoader()
        val model = this::class.java.getResourceAsStream("/bunny.obj").use { inp ->
            loader.loadModel("bunny", inp!!)
        } ?: Assertions.fail("Failed to load model.")

        /* Serialize and deserialize model. */
        val cachedModel3dContent = CachedModel3dContent(tmpPath, model)
        val file = File(tmpPath.toString())
        file.delete()

        /* Check if original and deserialized models are the same. */
        Assertions.assertEquals(model, cachedModel3dContent.content)
    }
}