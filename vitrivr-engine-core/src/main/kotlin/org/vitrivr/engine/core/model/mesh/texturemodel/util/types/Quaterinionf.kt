package org.vitrivr.engine.core.model.mesh.texturemodel.util.types

import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import kotlinx.serialization.Serializable
/**
 * Represents a quaternion, which is a complex number used to represent rotations in 3D space.
 */
@Serializable
class Quaternionf {
    var x: Float = 0f
    var y: Float = 0f
    var z: Float = 0f
    var w: Float = 0f

    /**
     * Default constructor - initializes the quaternion as an identity quaternion (no rotation).
     */
    constructor() {
        identity()
    }

    /**
     * Constructor with specified components.
     *
     * @param x The x-component of the quaternion.
     * @param y The y-component of the quaternion.
     * @param z The z-component of the quaternion.
     * @param w The w-component of the quaternion.
     */
    constructor(x: Float, y: Float, z: Float, w: Float) {
        this.x = x
        this.y = y
        this.z = z
        this.w = w
    }

    /**
     * Sets the quaternion to the identity quaternion.
     * The identity quaternion represents no rotation.
     *
     * @return This quaternion after being set to the identity quaternion.
     */
    fun identity(): Quaternionf {
        this.x = 0.0f
        this.y = 0.0f
        this.z = 0.0f
        this.w = 1.0f
        return this
    }

    /**
     * Normalizes the quaternion to ensure it represents a valid rotation.
     *
     * @return This quaternion after being normalized.
     */
    fun normalize(): Quaternionf {
        val length = sqrt((x * x + y * y + z * z + w * w).toDouble()).toFloat()
        if (length != 0.0f) {
            val invLength = 1.0f / length
            this.x *= invLength
            this.y *= invLength
            this.z *= invLength
            this.w *= invLength
        }
        return this
    }

    /**
     * Multiplies this quaternion by another quaternion.
     *
     * @param other The quaternion to multiply by.
     * @return This quaternion after the multiplication.
     */
    fun mul(other: Quaternionf): Quaternionf {
        val newX = this.w * other.x + (this.x * other.w) + (this.y * other.z) - this.z * other.y
        val newY = this.w * other.y + (this.y * other.w) + (this.z * other.x) - this.x * other.z
        val newZ = this.w * other.z + (this.z * other.w) + (this.x * other.y) - this.y * other.x
        val newW = this.w * other.w - (this.x * other.x) - (this.y * other.y) - (this.z * other.z)

        this.x = newX
        this.y = newY
        this.z = newZ
        this.w = newW

        return this
    }

    /**
     * Multiplies this quaternion by a scalar value.
     *
     * @param scalar The scalar value to multiply each component of this quaternion by.
     * @return This quaternion after being multiplied by the scalar.
     */
    fun mul(scalar: Float): Quaternionf {
        this.x *= scalar
        this.y *= scalar
        this.z *= scalar
        this.w *= scalar
        return this
    }

    /**
     * Computes the conjugate of this quaternion.
     * The conjugate is used to compute the inverse and is obtained by negating the x, y, and z components.
     *
     * @return The conjugate of this quaternion.
     */
    fun conjugate(): Quaternionf {
        this.x = -this.x
        this.y = -this.y
        this.z = -this.z
        return this
    }

    /**
     * Applies this quaternion to transform a vector.
     * This is useful for rotating vectors in 3D space.
     *
     * @param v The vector to be transformed.
     * @return The transformed vector.
     */
    fun transform(v: Vec3f): Vec3f {
        val vecQuat = Quaternionf(v.x, v.y, v.z, 0.0f)
        val resQuat: Quaternionf = this.mul(vecQuat).mul(this.conjugate())
        return Vec3f(resQuat.x, resQuat.y, resQuat.z)
    }

    /**
     * Sets the quaternion to represent a rotation around an axis in radians.
     *
     * @param axisX The x-component of the rotation axis.
     * @param axisY The y-component of the rotation axis.
     * @param axisZ The z-component of the rotation axis.
     * @param angle The angle of rotation in radians.
     * @return This quaternion after being set to represent the rotation.
     */
    fun fromAxisAngleRad(axisX: Float, axisY: Float, axisZ: Float, angle: Float): Quaternionf {
        val halfAngle = angle / 2.0f
        val sinHalfAngle = sin(halfAngle)
        x = axisX * sinHalfAngle
        y = axisY * sinHalfAngle
        z = axisZ * sinHalfAngle
        w = cos(halfAngle)
        return this
    }

    /**
     * Sets the quaternion to represent a rotation around an axis in radians.
     *
     * @param vector3f The axis of rotation as a vector.
     * @param angle The angle of rotation in radians.
     * @return This quaternion after being set to represent the rotation.
     */
    fun fromAxisAngleRad(vector3f: Vec3f, angle: Float): Quaternionf {
        val halfAngle = angle / 2.0f
        val sinHalfAngle = sin(halfAngle)
        x = vector3f.x * sinHalfAngle
        y = vector3f.y * sinHalfAngle
        z = vector3f.z * sinHalfAngle
        w = cos(halfAngle)
        return this
    }

    /**
     * Provides a string representation of the quaternion.
     *
     * @return A string representing the quaternion in the format "Quaternionf(x, y, z, w)".
     */
    override fun toString(): String {
        return "Quaternionf($x, $y, $z, $w)"
    }
}
