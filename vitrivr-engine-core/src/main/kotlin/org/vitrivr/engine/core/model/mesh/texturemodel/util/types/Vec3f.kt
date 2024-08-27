package org.vitrivr.engine.core.model.mesh.texturemodel.util.types

import kotlin.math.sqrt
import java.io.Serializable as JavaSerializable
import kotlinx.serialization.Serializable
/**
 * Represents a 3D vector with floating-point coordinates.
 * This class provides basic vector operations such as addition, subtraction, scaling, and normalization.
 */
@Serializable
class Vec3f
@JvmOverloads constructor(var x: Float = 0.0f, var y: Float = 0.0f, var z: Float = 0.0f) : JavaSerializable {

    /**
     * Copy constructor.
     *
     * @param other The vector to copy from.
     */
    constructor(other: Vec3f) : this(other.x, other.y, other.z)

    /**
     * Adds this vector to another vector.
     *
     * @param other The vector to add.
     * @return A new vector that is the result of the addition.
     */
    fun add(other: Vec3f): Vec3f {
        return Vec3f(this.x + other.x, this.y + other.y, this.z + other.z)
    }

    /**
     * Subtracts another vector from this vector.
     *
     * @param other The vector to subtract.
     * @return A new vector that is the result of the subtraction.
     */
    fun subtract(other: Vec3f): Vec3f {
        return Vec3f(this.x - other.x, this.y - other.y, this.z - other.z)
    }

    /**
     * Scales this vector by a scalar value.
     *
     * @param scalar The scalar value to scale by.
     * @return A new vector that is the result of the scaling.
     */
    fun scale(scalar: Float): Vec3f {
        return Vec3f(this.x * scalar, this.y * scalar, this.z * scalar)
    }

    /**
     * Performs element-wise multiplication with another vector.
     *
     * @param other The vector to multiply with.
     * @return A new vector that is the result of the element-wise multiplication.
     */
    fun mul(other: Vec3f): Vec3f {
        return Vec3f(this.x * other.x, this.y * other.y, this.z * other.z)
    }

    /**
     * Multiplies this vector by a scalar value.
     *
     * @param scalar The scalar value to multiply by.
     * @return This vector after being multiplied by the scalar.
     */
    fun mul(scalar: Float): Vec3f {
        x *= scalar
        y *= scalar
        z *= scalar
        return this
    }

    /**
     * Divides this vector by a scalar value.
     *
     * @param scalar The scalar value to divide by.
     * @return A new vector that is the result of the division.
     * @throws IllegalArgumentException if the scalar is zero.
     */
    fun div(scalar: Float): Vec3f {
        require(scalar != 0f) { "Cannot divide by zero." }
        return Vec3f(this.x / scalar, this.y / scalar, this.z / scalar)
    }

    /**
     * Performs element-wise division with another vector.
     *
     * @param other The vector to divide by.
     * @return A new vector that is the result of the element-wise division.
     */
    fun div(other: Vec3f): Vec3f {
        return Vec3f(this.x / other.x, this.y / other.y, this.z / other.z)
    }

    /**
     * Computes the cross product of this vector with another vector and stores the result in the given destination vector.
     *
     * @param v The vector to compute the cross product with.
     * @param dest The destination vector to store the result.
     * @return The destination vector with the cross product result.
     */
    fun cross(v: Vec3f, dest: Vec3f): Vec3f {
        val crossX: Float = this.y * v.z - this.z * v.y
        val crossY: Float = this.z * v.x - this.x * v.z
        val crossZ: Float = this.x * v.y - this.y * v.x

        dest.x = crossX
        dest.y = crossY
        dest.z = crossZ

        return dest
    }

    /**
     * Computes the magnitude (length) of the vector.
     *
     * @return The magnitude of the vector.
     */
    fun length(): Float {
        return sqrt((x * x + y * y + z * z).toDouble()).toFloat()
    }

    /**
     * Normalizes the vector to make it unit length.
     *
     * @return A new vector that is the normalized version of this vector.
     */
    fun normalize(): Vec3f {
        val length = length()
        if (length != 0f) {
            return Vec3f(this.x / length, this.y / length, this.z / length)
        }
        return Vec3f(0f, 0f, 0f)
    }

    /**
     * Sets the x, y, and z components of this vector to the given values.
     *
     * @param x The new x component.
     * @param y The new y component.
     * @param z The new z component.
     * @return This vector after setting the new values.
     */
    fun set(x: Float, y: Float, z: Float): Vec3f {
        this.x = x
        this.y = y
        this.z = z
        return this
    }

    /**
     * Sets the x, y, and z components of this vector to the values of another vector.
     *
     * @param other The vector to copy values from.
     * @return This vector after setting the new values.
     */
    fun set(other: Vec3f): Vec3f {
        return set(other.x, other.y, other.z)
    }

    /**
     * Sets all components of this vector to zero.
     *
     * @return A new vector with all components set to zero.
     */
    fun zero(): Vec3f {
        return Vec3f(0f, 0f, 0f)
    }

    /**
     * Provides a string representation of the vector.
     *
     * @return A string representing the vector in the format "Vec3f(x, y, z)".
     */
    override fun toString(): String {
        return "Vec3f($x, $y, $z)"
    }

    /**
     * Compares this vector to another object for equality.
     *
     * @param obj The object to compare to.
     * @return True if the object is a Vec3f with the same x, y, and z components, false otherwise.
     */
    override fun equals(obj: Any?): Boolean {
        if (this === obj) return true
        if (obj == null || javaClass != obj.javaClass) return false
        val vector = obj as Vec3f
        return java.lang.Float.compare(vector.x, x) == 0 && java.lang.Float.compare(
            vector.y, y
        ) == 0 && java.lang.Float.compare(vector.z, z) == 0
    }

    /**
     * Computes a hash code for this vector.
     *
     * @return A hash code value for the vector.
     */
    override fun hashCode(): Int {
        return java.lang.Float.hashCode(x) xor java.lang.Float.hashCode(y) xor java.lang.Float.hashCode(z)
    }

    /**
     * Computes the squared distance between this vector and another vector.
     *
     * @param v The vector to compute the distance to.
     * @return The squared distance between this vector and the other vector.
     */
    fun distanceSquared(v: Vec3f): Float {
        val dx = this.x - v.x
        val dy = this.y - v.y
        val dz = this.z - v.z
        return dx * dx + dy * dy + dz * dz
    }
}
