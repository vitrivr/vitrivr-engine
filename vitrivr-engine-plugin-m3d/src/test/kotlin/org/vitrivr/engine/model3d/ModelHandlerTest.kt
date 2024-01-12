package org.vitrivr.engine.model3d

import org.joml.Vector3f
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.nio.file.Files
import java.nio.file.StandardOpenOption

class ModelHandlerTest {
    @Test
    fun loadBunny() {
        val handler = ModelHandler()
        val path = Files.createTempFile("bunny", ".obj")
        Files.newOutputStream(path, StandardOpenOption.CREATE, StandardOpenOption.WRITE).use { out ->
            this::class.java.getResourceAsStream("/bunny.obj").use { inp ->
                out.write(inp!!.readAllBytes())
            }
        }

        /* Validate model */
        val model = handler.loadModel("test", path.toString())
        Assertions.assertTrue(model.getMaterials().size == 1)
        Assertions.assertTrue(model.getMaterials().first().meshes.size == 1)

        /* Validate mesh. */
        val mesh = model.getMaterials().first().meshes.first()
        Assertions.assertEquals(4968, mesh.numberOfFaces)
        Assertions.assertEquals(2503, mesh.numberOfVertices)
        //TODO: Maybe more tests?
    }
}