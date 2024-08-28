package org.vitrivr.engine.core.model.mesh.texturemodel.util.types

import java.io.Serializable

/**
 * Represents a 4D vector with floating-point coordinates.
 * This class provides various operations for manipulating and interacting with 4D vectors.
 *
 * @property x The x component of the vector.
 * @property y The y component of the vector.
 * @property z The z component of the vector.
 * @property w The w component of the vector.
 */
class Vec4f(
    var x: Float = 0.0f, var y: Float = 0.0f, var z: Float = 0.0f, var w: Float = 0.0f
) : Serializable {

    /**
     * Sets the components of this vector to the specified values.
     *
     * @param x The new x component.
     * @param y The new y component.
     * @param z The new z component.
     * @param w The new w component.
     * @return This vector with updated components.
     */
    fun set(x: Float, y: Float, z: Float, w: Float): Vec4f {
        this.x = x
        this.y = y
        this.z = z
        this.w = w
        return this
    }

    /**
     * Adds another vector to this vector.
     *
     * @param other The vector to add.
     * @return This vector after addition.
     */
    fun add(other: Vec4f): Vec4f {
        x += other.x
        y += other.y
        z += other.z
        w += other.w
        return this
    }

    /**
     * Subtracts another vector from this vector.
     *
     * @param other The vector to subtract.
     * @return This vector after subtraction.
     */
    fun sub(other: Vec4f): Vec4f {
        x -= other.x
        y -= other.y
        z -= other.z
        w -= other.w
        return this
    }

    /**
     * Multiplies this vector by a scalar.
     *
     * @param scalar The scalar value to multiply by.
     * @return This vector after scaling.
     */
    fun mul(scalar: Float): Vec4f {
        x *= scalar
        y *= scalar
        z *= scalar
        w *= scalar
        return this
    }

    /**
     * Divides this vector by a scalar.
     *
     * @param scalar The scalar value to divide by.
     * @return This vector after division.
     * @throws ArithmeticException if the scalar is zero.
     */
    fun div(scalar: Float): Vec4f {
        val invScalar = 1.0f / scalar
        x *= invScalar
        y *= invScalar
        z *= invScalar
        w *= invScalar
        return this
    }

    /**
     * Computes the length (magnitude) of the vector.
     *
     * @return The length of the vector.
     */
    fun length(): Float {
        return kotlin.math.sqrt(x * x + y * y + z * z + w * w)
    }

    /**
     * Normalizes the vector to make its length equal to 1.
     *
     * @return This vector after normalization.
     */
    fun normalize(): Vec4f {
        val len = length()
        if (len != 0.0f) {
            val invLen = 1.0f / len
            x *= invLen
            y *= invLen
            z *= invLen
            w *= invLen
        }
        return this
    }

    /**
     * Provides a string representation of the vector.
     *
     * @return A string representation in the format "Vector4f(x=x, y=y, z=z, w=w)".
     */
    override fun toString(): String {
        return "Vector4f(x=$x, y=$y, z=$z, w=$w)"
    }

    /**
     * Creates a copy of this vector.
     *
     * @return A new vector that is a copy of this vector.
     */
    fun copy(): Vec4f {
        return Vec4f(x, y, z, w)
    }

    /**
     * Checks if this vector is approximately equal to another vector.
     *
     * @param other The vector to compare with.
     * @param epsilon The tolerance for floating-point comparison.
     * @return True if the vectors are approximately equal, false otherwise.
     */
    fun equals(other: Vec4f, epsilon: Float = 0.000001f): Boolean {
        return kotlin.math.abs(x - other.x) < epsilon && kotlin.math.abs(y - other.y) < epsilon && kotlin.math.abs(z - other.z) < epsilon && kotlin.math.abs(
            w - other.w
        ) < epsilon
    }
}
