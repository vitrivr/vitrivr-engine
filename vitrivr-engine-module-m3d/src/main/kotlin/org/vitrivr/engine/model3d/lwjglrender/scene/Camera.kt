package org.vitrivr.engine.model3d.lwjglrender.scene

import org.joml.Matrix4f
import org.joml.Quaternionf
import org.joml.Vector2f
import org.joml.Vector3f

/**
 * Camera class for the LWJGL renderer.
 *
 *
 * This class is responsible for the camera position and orientation. It is used to calculate the view matrix for the renderer. It provides methods to move and rotate the camera.
 */
class Camera {
    /**
     * Helper Vector for X-Axis translation.
     */
    private val translativX = Vector3f()

    /**
     * Helper Vector for Y-Axis translation.
     */
    private val translativY = Vector3f()

    /**
     * Helper Vector for Z-Axis translation.
     */
    private val translativZ = Vector3f()
    /**
     * Returns the position of the camera.
     *
     * @return Position of the camera. (x,y,z)
     */
    /**
     * Position of the camera.
     */
    val position: Vector3f = Vector3f()

    /**
     * Rotation of the camera.
     */
    private val rotation = Vector2f()
    /**
     * Returns the view matrix of the camera.
     *
     * @return view matrix of the camera.
     */
    /**
     * Resulting view matrix.
     */
    val viewMatrix: Matrix4f = Matrix4f()

    /**
     * Helper Quaternion for orbit rotation.
     */
    private var orbitRotation: Quaternionf

    /**
     * Instantiates a new Camera. Initializes all helper vectors and matrices.
     */
    init {
        this.orbitRotation = Quaternionf()
    }

    /**
     * Rotates the camera by the given amount.
     *
     *
     * Camera stays aligned to the y plane.
     *
     * @param x Amount (rad) of rotation around the X-Axis.
     * @param y Amount (rad) of rotation around the Y-Axis.
     */
    @Suppress("unused")
    fun rotate(x: Float, y: Float) {
        rotation.add(x, y)
        this.recalculate()
    }

    /**
     * Moves the camera by the given amount.
     *
     * @param x Amount of movement along the X-Axis. + is right, - is left.
     * @param y Amount of movement along the Y-Axis. + is up, - is down.
     * @param z Amount of movement along the Z-Axis. + is forward, - is backward.
     */
    @Suppress("unused")
    private fun move(x: Float, y: Float, z: Float) {
        position.add(x, y, z)
        if (x > 0) {
            this.move(x, Direction.RIGHT)
        } else if (x < 0) {
            this.move(x, Direction.LEFT)
        }
        if (y > 0) {
            this.move(y, Direction.UP)
        } else if (y < 0) {
            this.move(y, Direction.DOWN)
        }
        if (z > 0) {
            this.move(z, Direction.FORWARD)
        } else if (z < 0) {
            this.move(z, Direction.BACKWARD)
        }
    }

    /**
     * Moves the camera by the given amount in the given direction.
     *
     * @param inc       Amount of movement.
     * @param direction Direction of movement.
     */
    fun move(inc: Float, direction: Direction?) {
        when (direction) {
            Direction.FORWARD -> {
                viewMatrix.positiveZ(this.translativZ).negate().mul(inc)
                position.add(this.translativZ)
            }

            Direction.BACKWARD -> {
                viewMatrix.positiveZ(this.translativZ).negate().mul(inc)
                position.sub(this.translativZ)
            }

            Direction.LEFT -> {
                viewMatrix.positiveX(this.translativX).mul(inc)
                position.sub(this.translativX)
            }

            Direction.RIGHT -> {
                viewMatrix.positiveX(this.translativX).mul(inc)
                position.add(this.translativX)
            }

            Direction.UP -> {
                viewMatrix.positiveY(this.translativY).mul(inc)
                position.add(this.translativY)
            }

            Direction.DOWN -> {
                viewMatrix.positiveY(this.translativY).mul(inc)
                position.sub(this.translativY)
            }

            null -> error("Direction is null")
        }
        this.recalculate()
    }

    /**
     * recalculates the view matrix.
     */
    private fun recalculate() {
        viewMatrix.identity()
            .rotate(this.orbitRotation)
            .translate(-position.x, -position.y, -position.z)
    }

    /**
     * Sets the absolute position of the camera.
     *
     * @param x Position along the X-Axis.
     * @param y Position along the Y-Axis.
     * @param z Position along the Z-Axis.
     * @return this
     */
    fun setPosition(x: Float, y: Float, z: Float): Camera {
        position[x, y] = z
        this.recalculate()
        return this
    }

    /**
     * Sets the absolute position of the camera.
     *
     * @param position Position of the camera. (x,y,z)
     * @return this
     */
    fun setPosition(position: Vector3f): Camera {
        return this.setPosition(position.x, position.y, position.z)
    }

    /**
     * Sets the absolute rotation of the camera.
     *
     * @param x (rad) Rotation around the X-Axis.
     * @param y (rad) Rotation around the Y-Axis.
     * @return this
     */
    fun setRotation(x: Float, y: Float): Camera {
        rotation[x] = y
        this.recalculate()
        return this
    }

    /**
     * Moves the orbit of the camera by the given amount.
     *
     * @param x Amount (rad) of rotation around the X-Axis.
     * @param y Amount (rad) of rotation around the Y-Axis.
     * @param z Amount (rad) of rotation around the Z-Axis.
     */
    fun moveOrbit(x: Float, y: Float, z: Float): Camera {
        var x = x
        var y = y
        var z = z
        y = (y.toDouble() * 2.0 * Math.PI).toFloat()
        x = (x.toDouble() * 2.0 * Math.PI).toFloat()
        z = (z.toDouble() * 2.0 * Math.PI).toFloat()
        orbitRotation.rotateYXZ(y, x, z)
        //this.viewMatrix.rotate(this.orbitRotation);
        this.recalculate()
        return this
    }

    /**
     * Sets the absolute orbit of the camera.
     *
     * @param x (rad) Rotation around the X-Axis.
     * @param y (rad) Rotation around the Y-Axis.
     * @param z (rad) Rotation around the Z-Axis.
     * @return this
     */
    fun setOrbit(x: Float, y: Float, z: Float): Camera {
        var x = x
        var y = y
        var z = z
        x = (x.toDouble() * 2.0 * Math.PI).toFloat()
        y = (y.toDouble() * 2.0 * Math.PI).toFloat()
        z = (z.toDouble() * 2.0 * Math.PI).toFloat()
        //this.orbitRotation.fromAxisAngleRad(x, y, z, angle);
        orbitRotation.rotationXYZ(x, y, z)
        //this.viewMatrix.rotate(this.orbitRotation);
        this.recalculate()
        return this
    }

    /**
     * Sets the position and the point the camera is looking at.
     *
     * @param cameraPosition Position of the camera. (x,y,z)
     * @param objectPosition Position of the point the camera is looking at.
     * @return this
     */
    fun setPositionAndOrientation(cameraPosition: Vector3f?, objectPosition: Vector3f?): Camera {
        val lookDir = Vector3f(objectPosition).sub(cameraPosition).normalize()
        val yNorm = Vector3f(0f, 1f, 0f).normalize()
        var right = Vector3f(lookDir).cross(yNorm).normalize()
        if (java.lang.Float.isNaN(right.x()) || java.lang.Float.isNaN(right.y()) || java.lang.Float.isNaN(right.y())) {
            right = Vector3f(1f, 0f, 0f)
        }
        val up = Vector3f(right).cross(lookDir).normalize()
        position.set(cameraPosition)
        this.orbitRotation = Quaternionf()
        orbitRotation.lookAlong(lookDir, up)
        this.recalculate()
        return this
    }

    /**
     * Sets the position and the point the camera is looking at. Furthermore, it sets the up vector of the camera.
     *
     * @param cameraPosition Position of the camera.
     * @param objectPosition Position of the point the camera is looking at.
     * @param up             Up vector of the camera.
     * @return this
     */
    fun setPositionAndOrientation(cameraPosition: Vector3f, objectPosition: Vector3f?, up: Vector3f?): Camera {
        val lookDir = Vector3f(objectPosition).sub(cameraPosition).normalize()

        this.setPosition(cameraPosition)
        orbitRotation.lookAlong(lookDir, up)
        this.recalculate()
        return this
    }


    /**
     * Helper method to handle the degrees over 360 and under 0.
     */
    @Suppress("unused")
    fun degreeHandler(degree: Float): Float {
        var degree = degree
        if (degree > 360) {
            degree -= 360f
        } else if (degree < 0) {
            degree += 360f
        }
        return degree
    }
}