package org.vitrivr.engine.model3d.lwjglrender.render

import org.joml.Vector4f
import org.joml.Matrix4f
import org.lwjgl.opengl.GL30
import org.vitrivr.engine.model3d.lwjglrender.glmodel.GLScene
import org.vitrivr.engine.model3d.lwjglrender.glmodel.GLTexture
import org.vitrivr.engine.model3d.lwjglrender.glmodel.IGLModel
import org.vitrivr.engine.model3d.lwjglrender.render.ShaderProgram.ShaderModuleData

/**
 * SceneRender
 *
 *  * Renders the scene
 *  * Loads the scene shader
 *  * Creates the uniforms
 *  * Binds the  Model
 *  * Binds the Texture
 *
 */
class SceneRender {
    /**
     * Instance of the scene shader program
     */
    private val shaderProgram: ShaderProgram

    /**
     * Uniforms for the scene shader
     */
    private var uniformsMap: UniformsMap? = null

    /**
     * SceneRender. During construction: Loads the scene shader from the resources
     */
    init {
        val shaderModuleDataList = ArrayList<ShaderModuleData>()
        shaderModuleDataList.add(ShaderModuleData(VERTEX_SHADER_PATH, GL30.GL_VERTEX_SHADER))
        shaderModuleDataList.add(ShaderModuleData(FRAGMENT_SHADER_PATH, GL30.GL_FRAGMENT_SHADER))
        this.shaderProgram = ShaderProgram(shaderModuleDataList)
        this.createUniforms()
    }

    /**
     * Creates the uniforms for the scene shader creates the following uniforms:
     *
     *  * projectionMatrix
     *  * modelMatrix
     *  * viewMatrix
     *  * txtSampler
     *  * material.diffuse
     *
     */
    private fun createUniforms() {
        this.uniformsMap = UniformsMap(shaderProgram.programId)
        uniformsMap!!.createUniform("projectionMatrix")
        uniformsMap!!.createUniform("modelMatrix")
        uniformsMap!!.createUniform("viewMatrix")
        uniformsMap!!.createUniform("txtSampler")
        uniformsMap!!.createUniform("material.diffuse")
    }

    /**
     * Releases all resources
     *
     *  * Releases the shader program
     *  * Releases the uniforms
     *
     */
    fun cleanup() {
        shaderProgram.cleanup()
        uniformsMap!!.cleanup()
        this.uniformsMap = null
    }

    /**
     * Renders the Models in the scene
     *
     *  * Binds projection matrix
     *  * Binds view matrix
     *  * Binds texture sampler
     *
     * Further, iterate over all models in the scene
     *
     *  * Iterate over all materials in the model
     *  * Sets texture or color function
     *  * Iterate over all meshes in the material
     *  * Binds the mesh
     *  * Iterate over all entities to draw the mesh
     *  * Binds the model matrix
     *  * Draws the mesh
     *  * Unbinds
     *
     * @param scene Scene to render
     * @param opt Render options
     */
    /**
     * Renders the Models in the scene
     * Creates standard render options
     * @param scene Scene to render
     */
    @JvmOverloads
    fun render(scene: GLScene, opt: RenderOptions = RenderOptions()) {
        shaderProgram.bind()

        uniformsMap!!.setUniform("projectionMatrix", scene.getProjection().projMatrix)
        uniformsMap!!.setUniform("viewMatrix", scene.getCamera().viewMatrix)
        uniformsMap!!.setUniform("txtSampler", 0)

        val models: Collection<IGLModel> = scene.getModels().values
        val textures = scene.getTextureCache()

        for (model in models) {
            val entities = model.getEntities()
            for (material in model.getMaterials()) {
                var texture: GLTexture
                // Either draw texture or use color function
                if (opt.showTextures) {
                    val v4f = Vector4f(material.diffuseColor.x, material.diffuseColor.y, material.diffuseColor.z, material.diffuseColor.w)
                    uniformsMap!!.setUniform("material.diffuse", v4f)
                    texture = textures.getTexture(material.texture.texturePath)!!
                } else {
                    val v4f = Vector4f(opt.colorfunction.apply(1f).x, opt.colorfunction.apply(1f).y, opt.colorfunction.apply(1f).z, opt.colorfunction.apply(1f).w)
                    uniformsMap!!.setUniform("material.diffuse", v4f)
                    texture = textures.getTexture("default")!!
                }
                GL30.glActiveTexture(GL30.GL_TEXTURE0)
                texture.bind()
                for (mesh in material.meshes) {
                    GL30.glBindVertexArray(mesh.vaoId)
                    for (entity in entities) {
                        val floatBuffer = entity.modelMatrix.getFloatBuffer()
                        val matrix4f = Matrix4f(floatBuffer)
                        uniformsMap!!.setUniform("modelMatrix", matrix4f)
                        GL30.glDrawElements(GL30.GL_TRIANGLES, mesh.numVertices, GL30.GL_UNSIGNED_INT, 0)
                    }
                }
            }
        }
        GL30.glBindVertexArray(0)

        shaderProgram.unbind()
    }

    companion object {
        /**
         * Resource path to the scene shader program
         */
        private const val VERTEX_SHADER_PATH = "/renderer/lwjgl/shaders/scene.vert"

        /**
         * Resource path to the fragment shader program
         */
        private const val FRAGMENT_SHADER_PATH = "/renderer/lwjgl/shaders/scene.frag"
    }
}