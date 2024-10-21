package org.vitrivr.engine.model3d.lwjglrender.renderer

import java.util.*
import java.util.concurrent.BlockingDeque
import kotlin.math.pow
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.joml.Vector3f
import org.lwjgl.system.Configuration
import org.vitrivr.engine.core.model.mesh.texturemodel.IModel
import org.vitrivr.engine.model3d.lwjglrender.util.datatype.Variant
import org.vitrivr.engine.model3d.lwjglrender.util.fsm.abstractworker.JobControlCommand
import org.vitrivr.engine.model3d.lwjglrender.util.fsm.abstractworker.StateEnter
import org.vitrivr.engine.model3d.lwjglrender.util.fsm.abstractworker.StateProvider
import org.vitrivr.engine.model3d.lwjglrender.util.fsm.abstractworker.Worker
import org.vitrivr.engine.model3d.lwjglrender.util.fsm.model.Action
import org.vitrivr.engine.model3d.lwjglrender.util.fsm.model.Graph
import org.vitrivr.engine.model3d.lwjglrender.util.fsm.model.State
import org.vitrivr.engine.model3d.lwjglrender.util.fsm.model.Transition
import org.vitrivr.engine.model3d.lwjglrender.window.WindowOptions
import org.vitrivr.engine.model3d.lwjglrender.render.RenderOptions

/**
 * The RenderWorker is a worker which is responsible for rendering a model.
 *
 * This worker implements all methods which are needed to do a RenderJob.
 *
 * It constructs a Graph which describes the states and transitions which a render worker can do.
 *
 * If a job throws an exception the worker will send a JobControlCommand.ERROR to the caller.
 * Furthermore, the worker will unload the model.
 *
 * Each rendered image will be sent to the caller.
 *
 * The worker initializes the LWJGL engine.
 *
 * @see LWJGLOffscreenRenderer
 * @see Worker
 */
@StateProvider
class RenderWorker(jobs: BlockingDeque<RenderJob>) : Worker<RenderJob>(jobs) {

  private var renderer: LWJGLOffscreenRenderer? = null

  init {
    Configuration.STACK_SIZE.set(2.0.pow(17).toInt())
    this.renderer = LWJGLOffscreenRenderer()
    val defaultOptions = WindowOptions()
    renderer!!.setWindowOptions(defaultOptions)
    renderer!!.startEngine()

    renderJobQueue = jobs
    LOGGER.trace("Initialized RenderWorker")
  }

  companion object {
    private val LOGGER: Logger = LogManager.getLogger(RenderWorker::class.java)
    private lateinit var renderJobQueue: BlockingDeque<RenderJob>

    /**
     * Static getter for the renderJobQueue. A caller can use get the queue submit new jobs to the
     * render worker.
     *
     * @return the render job queue
     */
    fun getRenderJobQueue(): BlockingDeque<RenderJob> {
      return renderJobQueue
    }
  }

  /** The render worker main thread. */
  override fun run() {
    super.run()
    LOGGER.trace("Running RenderWorker")
  }

  /**
   * Creates the graph for the RenderWorker.
   *
   * @return the graph
   */
  override fun createGraph(): Graph {
    return Graph(
      // Setup the graph for the RenderWorker
      hashMapOf(
        Transition(State(RenderStates.IDLE), Action(RenderActions.SETUP.name)) to State(RenderStates.INIT_WINDOW),
        Transition(State(RenderStates.INIT_WINDOW), Action(RenderActions.SETUP.name)) to State(RenderStates.LOAD_MODEL),
        Transition(State(RenderStates.LOAD_MODEL), Action(RenderActions.SETUP.name)) to State(RenderStates.INIT_RENDERER),
        Transition(State(RenderStates.LOAD_MODEL), Action(RenderActions.RENDER.name)) to State(RenderStates.RENDER),
        Transition(State(RenderStates.LOAD_MODEL), Action(RenderActions.LOOKAT.name)) to State(RenderStates.LOOKAT),
        Transition(State(RenderStates.LOAD_MODEL), Action(RenderActions.LOOKAT_FROM.name)) to State(RenderStates.LOOK_FROM_AT_O),
        Transition(State(RenderStates.INIT_RENDERER), Action(RenderActions.RENDER.name)) to State(RenderStates.RENDER),
        Transition(State(RenderStates.INIT_RENDERER), Action(RenderActions.LOOKAT.name)) to State(RenderStates.LOOKAT),
        Transition(State(RenderStates.INIT_RENDERER), Action(RenderActions.LOOKAT_FROM.name)) to State(RenderStates.LOOK_FROM_AT_O),
        Transition(State(RenderStates.RENDER), Action(RenderActions.ROTATE.name)) to State(RenderStates.ROTATE),
        Transition(State(RenderStates.RENDER), Action(RenderActions.LOOKAT.name)) to State(RenderStates.LOOKAT),
        Transition(State(RenderStates.RENDER), Action(RenderActions.LOOKAT_FROM.name)) to State(RenderStates.LOOK_FROM_AT_O),
        Transition(State(RenderStates.RENDER), Action(RenderActions.SETUP.name)) to State(RenderStates.UNLOAD_MODEL),
        Transition(State(RenderStates.ROTATE), Action(RenderActions.RENDER.name)) to State(RenderStates.RENDER),
        Transition(State(RenderStates.LOOKAT), Action(RenderActions.RENDER.name)) to State(RenderStates.RENDER),
        Transition(State(RenderStates.LOOK_FROM_AT_O), Action(RenderActions.RENDER.name)) to State(RenderStates.RENDER)
      ),
      State(RenderStates.IDLE),
      hashSetOf(State(RenderStates.UNLOAD_MODEL))
    )
  }


  /**
   * Handler for render exceptions. Unloads the model and sends a JobControlCommand.ERROR to the
   * caller.
   *
   * @param ex The exception that was thrown.
   * @return The handler message.
   */
  override fun onJobException(ex: Exception?): String? {
    this.unload()
    this.currentJob?.putResultQueue(RenderJob(JobControlCommand.JOB_FAILURE))
    return "Job failed"
  }

  /** Initializes the renderer. Sets the window options and starts the engine. */
  @StateEnter(state = RenderStates.INIT_WINDOW, data = [RenderData.WINDOWS_OPTIONS])
  fun setWindowOptions(opt: WindowOptions) {
    LOGGER.trace("INIT_WINDOW RenderWorker")
    this.renderer = LWJGLOffscreenRenderer()

    renderer!!.setWindowOptions(opt)
    renderer!!.startEngine()
  }

  /** Sets specific render options. */
  @StateEnter(state = RenderStates.INIT_RENDERER, data = [RenderData.RENDER_OPTIONS])
  fun setRendererOptions(opt: RenderOptions) {
    LOGGER.trace("INIT_RENDERER RenderWorker")
    this.renderer!!.setRenderOptions(opt)
  }

  /** State to wait for new jobs. */
  @StateEnter(state = RenderStates.IDLE)
  fun idle() {
    LOGGER.trace("IDLE RenderWorker")
  }

  /**
   * Register a model to the renderer.
   *
   * @param model The model to register and to be rendered.
   */
  @StateEnter(state = RenderStates.LOAD_MODEL, data = [RenderData.MODEL])
  fun registerModel(model: IModel) {
    LOGGER.trace("LOAD_MODEL RenderWorker")
    this.renderer!!.assemble(model)
  }

  /** Renders the model. Sends the rendered image to the caller. */
  @StateEnter(state = RenderStates.RENDER)
  fun renderModel() {
    LOGGER.trace("RENDER RenderWorker")
    this.renderer!!.render()
    val pic = this.renderer!!.obtain()
    val data = Variant().set(RenderData.IMAGE, pic)
    val responseJob = RenderJob(data)
    this.currentJob?.putResultQueue(responseJob)
  }

  /**
   * Rotates the camera.
   *
   * @param rotation The rotation vector (x,y,z)
   */
  @StateEnter(state = RenderStates.ROTATE, data = [RenderData.VECTOR])
  fun rotate(rotation: Vector3f) {
    LOGGER.trace("ROTATE RenderWorker")
    this.renderer!!.moveCameraOrbit(rotation.x, rotation.y, rotation.z)
  }

  /**
   * Looks at the origin from a specific position. The rotation is not affected. Removes the
   * processed position vector from the list.
   *
   * @param vectors The list of position vectors
   */
  @StateEnter(state = RenderStates.LOOKAT, data = [RenderData.VECTORS])
  fun lookAt(vectors: LinkedList<Vector3f>) {
    LOGGER.trace("LOOKAT RenderWorker")
    val vec = vectors.poll()
    requireNotNull(vec)
    this.renderer!!.setCameraOrbit(vec.x, vec.y, vec.z)
  }

  /**
   * Looks from a specific position at the origin. Removes the processed position vector from the
   * list.
   *
   * @param vectors The list of position vectors
   */
  @StateEnter(state = RenderStates.LOOK_FROM_AT_O, data = [RenderData.VECTORS])
  fun lookFromAtO(vectors: LinkedList<Vector3f>) {
    LOGGER.trace("LOOK_FROM_AT_O RenderWorker")
    val vec = vectors.poll()
    requireNotNull(vec)
    this.renderer!!.lookFromAtO(vec.x, vec.y, vec.z)
  }

  /** Unloads the model and sends a JobControlCommand.JOB_DONE to the caller. */
  @StateEnter(state = RenderStates.UNLOAD_MODEL)
  fun unload() {
    LOGGER.trace("UNLOAD_MODEL RenderWorker")
    this.renderer!!.clear()
    this.renderer = null
    val responseJob = RenderJob(JobControlCommand.JOB_DONE)
    this.currentJob?.putResultQueue(responseJob)
  }

  private fun hashtableOf(vararg pairs: Pair<Transition, State>): Hashtable<Transition, State> {
    return Hashtable<Transition, State>().apply { pairs.forEach { (k, v) -> put(k, v) } }
  }
}
