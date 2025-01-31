package org.vitrivr.engine.model3d.lwjglrender.render

import org.lwjgl.opengl.GL30
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Paths
import java.util.function.Consumer

/**
 * ShaderProgram
 * Loads a shader program from the resources to the GL context
 */
class ShaderProgram(shaderModuleDataList: List<ShaderModuleData>) {
    /**
     * Returns the program id
     * @return program id
     */
    /**
     * Shader Program ID, is used to bind and release the program from the GL context
     */
    val programId: Int = GL30.glCreateProgram()

    /**
     * Creates a new ShaderProgram
     * Takes a list of ShaderModuleData (usually from Scene Renderer which loads the shaders from the resources during construction
     * Creates a new ShaderProgram in GL context, links the shaders and validates the program
     * For Shader creation, the following steps are performed:
     *
     *  * Reads the shader file
     *  * Creates a new shader in the GL context [ShaderProgram.createShader]
     *  * Compiles the shader
     *  * Attaches the shader to the program
     *  * Links the program
     *  * Binds the program to the GL context
     *
     * @param shaderModuleDataList List of ShaderModuleData
     */
    init {
        if (this.programId == 0) {
            throw RuntimeException("Could not create shader.")
        }
        val shaderModules = ArrayList<Int>()
        shaderModuleDataList.forEach(Consumer { s: ShaderModuleData ->
            shaderModules.add(
                this.createShader(
                    readShaderFile(s.shaderFile),
                    s.shaderType
                )
            )
        })
        this.link(shaderModules)
    }

    /**
     * Binds the ShaderProgram to the GL context
     */
    fun bind() {
        GL30.glUseProgram(this.programId)
    }

    /**
     * Unbinds the ShaderProgram from the GL context
     */
    fun unbind() {
        GL30.glUseProgram(0)
    }

    /**
     * Unbinds the ShaderProgram from the GL context
     * Deletes the ShaderProgram from the GL context
     */
    fun cleanup() {
        this.unbind()
        if (this.programId != 0) {
            GL30.glDeleteProgram(this.programId)
        }
    }

    /**
     * Links the program
     * Deletes the shaders
     * @param shaderModules List of shader ids
     */
    private fun link(shaderModules: List<Int>) {
        GL30.glLinkProgram(this.programId)
        if (GL30.glGetProgrami(this.programId, GL30.GL_LINK_STATUS) == 0) {
            throw RuntimeException("Error linking Shader")
        }
        shaderModules.forEach(Consumer { s: Int? ->
            GL30.glDetachShader(
                this.programId,
                s!!
            )
        })
        shaderModules.forEach(Consumer { shader: Int? ->
            GL30.glDeleteShader(
                shader!!
            )
        })
        //this.validate();
    }

    /**
     * Validates the program
     * Throws an exception if the program is not valid
     */
    fun validate() {
        GL30.glValidateProgram(this.programId)
        if (GL30.glGetProgrami(this.programId, GL30.GL_VALIDATE_STATUS) == 0) {
            throw RuntimeException("Error validate Shader")
        }
    }

    /**
     * Creates a new Shader in the GL context
     * Compiles the shader
     * Attaches the shader to the program
     * @return the shader id
     */
    protected fun createShader(shaderCode: String?, shaderType: Int): Int {
        val shaderId = GL30.glCreateShader(shaderType)
        check(shaderId != 0) { "Error creating shader" }
        GL30.glShaderSource(shaderId, shaderCode ?: "")
        GL30.glCompileShader(shaderId)

        check(GL30.glGetShaderi(shaderId, GL30.GL_COMPILE_STATUS) != 0) { "Error compiling shader" }
        GL30.glAttachShader(this.programId, shaderId)
        return shaderId
    }

    /**
     * RECORD for ShaderModuleData
     * @param shaderFile
     * @param shaderType
     */
    @JvmRecord
    data class ShaderModuleData(val shaderFile: String, val shaderType: Int)
    companion object {
        /**
         * Reads the shader file.
         *
         * @param filePath Path to the shader file
         * @return String containing the shader code
         */
        fun readShaderFile(filePath: String): String {
            var shader: String? = null

            /* First try: Load from resources. */
            try {
                ShaderProgram::class.java.getResourceAsStream(filePath).use { stream ->
                    if (stream != null) {
                        shader = String(stream.readAllBytes())
                    }
                }
            } catch (e: IOException) {
                /* No op. */
            }

            /* Second attempt try: Load from resources. */
            if (shader == null) {
                try {
                    shader = String(Files.readAllBytes(Paths.get(filePath)))
                } catch (ex: IOException) {
                    /* No op. */
                }
            }

            /* Make sure shader has been loaded. */
            checkNotNull(shader) { "Error reading shader file: $filePath" }
            return shader as String
        }
    }
}