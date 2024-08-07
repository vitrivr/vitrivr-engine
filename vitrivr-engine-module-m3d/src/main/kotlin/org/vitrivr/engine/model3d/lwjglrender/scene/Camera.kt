package org.vitrivr.engine.model3d.lwjglrender.scene

import org.joml.Matrix4f
import org.joml.Quaternionf
import org.joml.Vector2f
import org.joml.Vector3f

/**
 * Camera data class for the LWJGL renderer.
 *
 * This class is responsible for the camera position and orientation. It is used to calculate the view matrix for the renderer.
 */
data class Camera(
    val position: Vector3f = Vector3f(),
    val rotation: Vector2f = Vector2f(),
    val viewMatrix: Matrix4f = Matrix4f(),
    var orbitRotation: Quaternionf = Quaternionf()
) {
    /**
     * Helper Vector for X-Axis translation.
     */
    val translativX = Vector3f()

    /**
     * Helper Vector for Y-Axis translation.
     */
    val translativY = Vector3f()

    /**
     * Helper Vector for Z-Axis translation.
     */
    val translativZ = Vector3f()
}

/**
 * Rotates the camera by the given amount.
 *
 * Camera stays aligned to the y plane.
 *
 * @param x Amount (rad) of rotation around the X-Axis.
 * @param y Amount (rad) of rotation around the Y-Axis.
 */
fun Camera.rotate(x: Float, y: Float) {
    this.rotation.add(x, y)
    this.recalculate()
}

/**
 * Moves the camera by the given amount.
 *
 * @param x Amount of movement along the X-Axis. + is right, - is left.
 * @param y Amount of movement along the Y-Axis. + is up, - is down.
 * @param z Amount of movement along the Z-Axis. + is forward, - is backward.
 */
fun Camera.move(x: Float, y: Float, z: Float) {
    this.position.add(x, y, z)
    when {
        x > 0 -> this.move(x, Direction.RIGHT)
        x < 0 -> this.move(x, Direction.LEFT)
    }
    when {
        y > 0 -> this.move(y, Direction.UP)
        y < 0 -> this.move(y, Direction.DOWN)
    }
    when {
        z > 0 -> this.move(z, Direction.FORWARD)
        z < 0 -> this.move(z, Direction.BACKWARD)
    }
}

/**
 * Moves the camera by the given amount in the given direction.
 *
 * @param inc Amount of movement.
 * @param direction Direction of movement.
 */
fun Camera.move(inc: Float, direction: Direction) {
    when (direction) {
        Direction.FORWARD -> {
            this.viewMatrix.positiveZ(this.translativZ).negate().mul(inc)
            this.position.add(this.translativZ)
        }
        Direction.BACKWARD -> {
            this.viewMatrix.positiveZ(this.translativZ).negate().mul(inc)
            this.position.sub(this.translativZ)
        }
        Direction.LEFT -> {
            this.viewMatrix.positiveX(this.translativX).mul(inc)
            this.position.sub(this.translativX)
        }
        Direction.RIGHT -> {
            this.viewMatrix.positiveX(this.translativX).mul(inc)
            this.position.add(this.translativX)
        }
        Direction.UP -> {
            this.viewMatrix.positiveY(this.translativY).mul(inc)
            this.position.add(this.translativY)
        }
        Direction.DOWN -> {
            this.viewMatrix.positiveY(this.translativY).mul(inc)
            this.position.sub(this.translativY)
        }
    }
    this.recalculate()
}

/**
 * Recalculates the view matrix.
 */
fun Camera.recalculate() {
    this.viewMatrix.identity()
        .rotate(this.orbitRotation)
        .translate(-this.position.x, -this.position.y, -this.position.z)
}

/**
 * Sets the absolute position of the camera.
 *
 * @param x Position along the X-Axis.
 * @param y Position along the Y-Axis.
 * @param z Position along the Z-Axis.
 * @return this
 */
fun Camera.setPosition(x: Float, y: Float, z: Float): Camera {
    this.position.set(x, y, z)
    this.recalculate()
    return this
}

/**
 * Sets the absolute position of the camera.
 *
 * @param position Position of the camera. (x,y,z)
 * @return this
 */
fun Camera.setPosition(position: Vector3f): Camera {
    return this.setPosition(position.x, position.y, position.z)
}

/**
 * Sets the absolute rotation of the camera.
 *
 * @param x (rad) Rotation around the X-Axis.
 * @param y (rad) Rotation around the Y-Axis.
 * @return this
 */
fun Camera.setRotation(x: Float, y: Float): Camera {
    this.rotation.set(x, y)
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
fun Camera.moveOrbit(x: Float, y: Float, z: Float): Camera {
    val adjustedX = (x.toDouble() * 2.0 * Math.PI).toFloat()
    val adjustedY = (y.toDouble() * 2.0 * Math.PI).toFloat()
    val adjustedZ = (z.toDouble() * 2.0 * Math.PI).toFloat()
    this.orbitRotation.rotateYXZ(adjustedY, adjustedX, adjustedZ)
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
fun Camera.setOrbit(x: Float, y: Float, z: Float): Camera {
    val adjustedX = (x.toDouble() * 2.0 * Math.PI).toFloat()
    val adjustedY = (y.toDouble() * 2.0 * Math.PI).toFloat()
    val adjustedZ = (z.toDouble() * 2.0 * Math.PI).toFloat()
    this.orbitRotation.rotationXYZ(adjustedX, adjustedY, adjustedZ)
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
fun Camera.setPositionAndOrientation(cameraPosition: Vector3f, objectPosition: Vector3f): Camera {
    val lookDir = Vector3f(objectPosition).sub(cameraPosition).normalize()
    val yNorm = Vector3f(0f, 1f, 0f).normalize()
    var right = Vector3f(lookDir).cross(yNorm).normalize()
    if (java.lang.Float.isNaN(right.x()) || java.lang.Float.isNaN(right.y()) || java.lang.Float.isNaN(right.z())) {
        right = Vector3f(1f, 0f, 0f)
    }
    val up = Vector3f(right).cross(lookDir).normalize()
    this.position.set(cameraPosition)
    this.orbitRotation = Quaternionf().lookAlong(lookDir, up)
    this.recalculate()
    return this
}

/**
 * Sets the position and the point the camera is looking at. Furthermore, it sets the up vector of the camera.
 *
 * @param cameraPosition Position of the camera.
 * @param objectPosition Position of the point the camera is looking at.
 * @param up Up vector of the camera.
 * @return this
 */
fun Camera.setPositionAndOrientation(cameraPosition: Vector3f, objectPosition: Vector3f, up: Vector3f): Camera {
    val lookDir = Vector3f(objectPosition).sub(cameraPosition).normalize()
    this.position.set(cameraPosition)
    this.orbitRotation.lookAlong(lookDir, up)
    this.recalculate()
    return this
}

/**
 * Helper method to handle the degrees over 360 and under 0.
 */
fun Camera.degreeHandler(degree: Float): Float {
    var adjustedDegree = degree
    if (adjustedDegree > 360) {
        adjustedDegree -= 360f
    } else if (adjustedDegree < 0) {
        adjustedDegree += 360f
    }
    return adjustedDegree
}
