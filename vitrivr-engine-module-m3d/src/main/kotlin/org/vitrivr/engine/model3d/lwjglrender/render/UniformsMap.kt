package org.vitrivr.engine.model3d.lwjglrender.render

import org.joml.Matrix4f
import org.joml.Vector4f
import org.lwjgl.opengl.GL30
import org.lwjgl.system.MemoryStack

/**
 * Holds a hashmap for used uniforms are global variables in the shader e.g. projectionMatrix, modelMatrix, viewMatrix, txtSampler, material.diffuse
 *
 * @see [](https://www.khronos.org/opengl/wiki/Uniform_
@see <a href= )/resources/renderer/lwjgl/shaders/scene.vert">"./resources/renderer/lwjgl/shaders/scene.vert"
 *
 * @see ["./resources/renderer/lwjgl/shaders/scene.frag"](./resources/renderer/lwjgl/shaders/scene.frag)
 */
class UniformsMap(
    /**
     * Program id of the shader
     */
    private val programId: Int
) {
    /**
     * HashMap for the uniforms Key: Uniform name Value: Uniform location in the shader
     */
    private val uniforms = HashMap<String, Int>()

    /**
     * Creates a new uniform
     *
     * @param uniformName Name of the uniform
     */
    fun createUniform(uniformName: String) {
        val uniformLocation = GL30.glGetUniformLocation(this.programId, uniformName)
        if (uniformLocation < 0) {
            throw RuntimeException("Could not find uniform:$uniformName")
        }
        uniforms[uniformName] = uniformLocation
    }

    /**
     * Sets the value of a uniform to gl context
     *
     * @param uniformName Name of the uniform
     * @param value       Value of the uniform
     */
    fun setUniform(uniformName: String, value: Int) {
        GL30.glUniform1i(this.getUniformLocation(uniformName), value)
    }

    /**
     * Returns the location of the uniform from the hashmap
     *
     * @param uniformName name of the uniform
     * @return location of the uniform
     */
    private fun getUniformLocation(uniformName: String): Int {
        val location = uniforms[uniformName]
            ?: throw RuntimeException("Could not find uniform:$uniformName")
        return location
    }

    /**
     * Sets the value 4 float vector of a uniform to gl context
     *
     * @param uniformName Name of the uniform
     * @param value       Value of the uniform
     */
    fun setUniform(uniformName: String, value: Vector4f) {
        GL30.glUniform4f(this.getUniformLocation(uniformName), value.x, value.y, value.z, value.w)
    }

    /**
     * Sets the value 4*4 float matrix of a uniform to gl context
     *
     * @param uniformName Name of the uniform
     * @param value       Value of the uniform
     */
    fun setUniform(uniformName: String, value: Matrix4f) {
        MemoryStack.stackPush().use { memoryStack ->
            val location = uniforms[uniformName]
                ?: throw RuntimeException("Could not find uniform:$uniformName")
            GL30.glUniformMatrix4fv(location, false, value[memoryStack.mallocFloat(16)])
        }
    }

    /**
     * Cleans up the uniforms
     */
    fun cleanup() {
        uniforms.clear()
    }
}