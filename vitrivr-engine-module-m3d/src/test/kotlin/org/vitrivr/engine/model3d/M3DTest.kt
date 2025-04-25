package org.vitrivr.engine.model3d

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.vitrivr.engine.model3d.renderer.ExternalRenderer

/**
 * Unit tests to check the functionality of the [ModelLoader].
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
class M3DTest {
    /**
     * Tests if the Standford Bunny model can be loaded.
     */
    @Test
    fun loadBunny() {
        /* Load model. */
        val loader = ModelLoader()
        val model = this::class.java.getResourceAsStream("/bunny.obj").use { inp ->
            loader.loadModel("bunny", inp!!)
        } ?: Assertions.fail("Failed to load model.")

        /* Check if model was loaded correctly. */
        Assertions.assertTrue(model.getMaterials().size == 1)
        Assertions.assertTrue(model.getAllNormals().size == 4968)
    }

    /**
     * Tests if the Standford Bunny model can be loaded.
     */
    @Test
    fun renderBunny() {
        /* Load model. */
        val loader = ModelLoader()
        val model = this::class.java.getResourceAsStream("/bunny.obj").use { inp ->
            loader.loadModel("bunny", inp!!)
        } ?: Assertions.fail("Failed to load model.")

        /* Render image. */
        val renderer = ExternalRenderer()
        Assertions.assertDoesNotThrow() {
            ModelPreviewExporter.renderPreviewJPEG(model, renderer, 1.0f)
        }
    }
}