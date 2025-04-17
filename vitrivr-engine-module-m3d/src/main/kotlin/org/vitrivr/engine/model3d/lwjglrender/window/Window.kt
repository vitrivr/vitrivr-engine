package org.vitrivr.engine.model3d.lwjglrender.window

import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.lwjgl.glfw.*
import org.lwjgl.glfw.GLFW.GLFW_COCOA_RETINA_FRAMEBUFFER
import org.lwjgl.glfw.GLFW.GLFW_FALSE
import org.lwjgl.opengl.GL
import org.lwjgl.opengl.GL30
import org.lwjgl.system.MemoryUtil
import java.util.concurrent.Callable

/**
 * This class represents a window that can be used for rendering.
 * It is based on the LWJGL3 library.
 * It can be used for both headless and visible rendering.
 */
class Window(
    title: String?, opts: WindowOptions,
    /**
     * The function that is called when the window is resized.
     */
    private val resizeFunc: Callable<Void>
) {
    /**
     * The handle to the window.
     */
    private val windowHandle: Long
    /**
     * Returns the height of the window.
     *
     * @return Height of the window.
     */
    /**
     * The height of the window.
     */
    var height: Int
        private set
    /**
     * Returns the width of the window.
     *
     * @return Width of the window.
     */
    /**
     * The width of the window.
     */
    var width: Int
        private set

    /**
     * Constructor for Window.
     *
     * @param title Title of the window.
     * @param opts  Options for the window.
     * @param resizeFunc Function that is called when the window is resized.
     */
    init {
        LOGGER.trace("Try creating window '{}'...", title)
        check(GLFW.glfwInit()) { "Unable to initialize GLFW" }
        LOGGER.trace("GLFW initialized")

        GLFW.glfwDefaultWindowHints()
        // Window should be invisible for basic rendering
        GLFW.glfwWindowHint(GLFW.GLFW_VISIBLE, GL30.GL_FALSE)
        // Setting for headless rendering with MESA and Xvfb
        // See: https://github.com/vitrivr/cineast/blob/e5587fce1b5675ca9f6dbbfd5c17eb1880a98ce3/README.md
        //GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_CREATION_API, GLFW.GLFW_OSMESA_CONTEXT_API);
        // Switch off resize callback
        GLFW.glfwWindowHint(GLFW.GLFW_RESIZABLE, GL30.GL_FALSE)

        // Sets the OpenGL version number to MAJOR.MINOR e.g. 3.2
        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MAJOR, 3)
        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MINOR, 2)

        // Depending on the Options, the OpenGL profile can be set to either CORE or COMPAT (or ANY)
        // GLFW_OPENGL_COMPAT_PROFILE keeps the outdated functionality
        // GLFW_OPENGL_CORE_PROFILE removes the deprecated functionality
        // GLFW_OPENGL_ANY_PROFILE is used for version 3.2 and below
        if (opts.compatibleProfile) {
            GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_PROFILE, GLFW.GLFW_OPENGL_COMPAT_PROFILE)
        } else {
            GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_PROFILE, GLFW.GLFW_OPENGL_CORE_PROFILE)
            GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_FORWARD_COMPAT, GL30.GL_TRUE)
        }

        // Set window size if set in options, otherwise use maximum size of primary monitor
        if (opts.width > 0 && opts.height > 0) {
            this.width = opts.width
            this.height = opts.height
        } else {
            GLFW.glfwWindowHint(GLFW.GLFW_MAXIMIZED, GLFW.GLFW_TRUE)
            val vidMode = checkNotNull(GLFW.glfwGetVideoMode(GLFW.glfwGetPrimaryMonitor()))
            this.width = vidMode.width()
            this.height = vidMode.height()
        }

        LOGGER.trace(
            "Try creating window '{}' with size {}x{}...", title,
            this.width,
            this.height
        )
        this.windowHandle = GLFW.glfwCreateWindow(this.width, this.height, title ?: "Untitled", MemoryUtil.NULL, MemoryUtil.NULL)
        if (this.windowHandle == MemoryUtil.NULL) {
            throw RuntimeException("Failed to create the GLFW window")
        }

        // Setup the callbacks for the glfw window.
        // Resize and key Callback are not used for headless rendering.
        val resizeCallback = GLFW.glfwSetFramebufferSizeCallback(
            this.windowHandle
        ) { window: Long, w: Int, h: Int ->
            this.resized(
                w,
                h
            )
        }

        val errorCallback = GLFW.glfwSetErrorCallback { errorCode: Int, msgPtr: Long ->
            LOGGER.error(
                "Error code [{}], msg [{}]",
                errorCode,
                MemoryUtil.memUTF8(msgPtr)
            )
        }

        val keyCallback = GLFW.glfwSetKeyCallback(
            this.windowHandle
        ) { window: Long, key: Int, scancode: Int, action: Int, mods: Int ->
            if (key == GLFW.GLFW_KEY_ESCAPE && action == GLFW.GLFW_RELEASE) {
                GLFW.glfwSetWindowShouldClose(window, true) // We will detect this in the rendering loop
            }
        }

        GLFW.glfwMakeContextCurrent(this.windowHandle)
        GL.createCapabilities()

        val fbWidth = IntArray(1)
        val fbHeight = IntArray(1)
        GLFW.glfwGetFramebufferSize(this.windowHandle, fbWidth, fbHeight)
        GL30.glViewport(0, 0, fbWidth[0], fbHeight[0])


        if (opts.fps > 0) {
            GLFW.glfwSwapInterval(0)
        } else {
            GLFW.glfwSwapInterval(1)
        }

        // Set the window to be visible if not headless rendering
        if (!opts.hideWindow) {
            GLFW.glfwShowWindow(this.windowHandle)
        }
    }

    /**
     * Removes all callbacks and destroys the window.
     */
    fun cleanup() {
        Callbacks.glfwFreeCallbacks(this.windowHandle)
        GLFW.glfwDestroyWindow(this.windowHandle)
        GLFW.glfwTerminate()
        val callback = GLFW.glfwSetErrorCallback(null)
        callback?.free()
    }

    /**
     * Checks if a key is pressed.
     * @param keyCode Key code to check.
     * @return True if key is pressed, false otherwise.
     */
    fun isKeyPressed(keyCode: Int): Boolean {
        return GLFW.glfwGetKey(this.windowHandle, keyCode) == GLFW.GLFW_PRESS
    }

    /**
     * polls all pending events.
     */
    fun pollEvents() {
        GLFW.glfwPollEvents()
    }

    /**
     * Callback for window resize.
     *
     * @param width New width of the window.
     * @param height New height of the window.
     */
    protected fun resized(width: Int, height: Int) {
        this.width = width
        this.height = height

        GL30.glViewport(0, 0, width, height)

        try {
            resizeFunc.call()
        } catch (ex: Exception) {
            LOGGER.error("Error calling resize callback", ex)
        }
    }

    /**
     * Updates the window.
     */
    fun update() {
        GLFW.glfwSwapBuffers(this.windowHandle)
    }

    /**
     * Indicates if the window should be closed.
     */
    fun windowShouldClose(): Boolean {
        return GLFW.glfwWindowShouldClose(this.windowHandle)
    }

    companion object {
        private val LOGGER: Logger = LogManager.getLogger()
    }
}