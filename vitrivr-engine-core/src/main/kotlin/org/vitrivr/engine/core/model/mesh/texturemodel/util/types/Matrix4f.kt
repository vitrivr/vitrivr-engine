package org.vitrivr.engine.core.model.mesh.texturemodel.util.types

import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

/**
 * Represents a 4x4 matrix, commonly used in 3D graphics for transformations including translation, scaling, and rotation.
 */
class Matrix4f {

    /**
     * The matrix is stored as a single array of 16 floating-point elements in column-major order.
     */
    var m: FloatArray // 4x4 matrix stored in a single array of 16 elements

    /**
     * Constructor - initializes the matrix as an identity matrix.
     */
    init {
        m = FloatArray(16)
        identity()
    }

    /**
     * Sets the matrix to the identity matrix.
     * The identity matrix is a special type of matrix that does not alter the vector it multiplies.
     * The matrix has 1s on the diagonal and 0s elsewhere.
     *
     * @return This matrix after being set to the identity matrix.
     */
    fun identity(): Matrix4f {
        m[0] = 1f
        m[1] = 0f
        m[2] = 0f
        m[3] = 0f
        m[4] = 0f
        m[5] = 1f
        m[6] = 0f
        m[7] = 0f
        m[8] = 0f
        m[9] = 0f
        m[10] = 1f
        m[11] = 0f
        m[12] = 0f
        m[13] = 0f
        m[14] = 0f
        m[15] = 1f
        return this
    }

    /**
     * Multiplies this matrix by another matrix and stores the result in this matrix.
     *
     * @param other The matrix to multiply this matrix by.
     * @return This matrix after being multiplied by the specified matrix.
     */
    fun mul(other: Matrix4f): Matrix4f {
        val result = FloatArray(16)

        for (row in 0..3) {
            for (col in 0..3) {
                result[row * 4 + col] =
                    m[row * 4 + 0] * other.m[0 * 4 + col] + m[row * 4 + 1] * other.m[1 * 4 + col] + m[row * 4 + 2] * other.m[2 * 4 + col] + m[row * 4 + 3] * other.m[3 * 4 + col]
            }
        }

        m = result
        return this
    }

    /**
     * Multiplies this matrix by a scalar value.
     *
     * @param scalar The scalar value to multiply each element of this matrix by.
     * @return This matrix after being multiplied by the scalar.
     */
    fun mul(scalar: Float): Matrix4f {
        for (i in 0..15) {
            m[i] *= scalar
        }
        return this
    }


    /**
     * Sets this matrix to a scaling matrix.
     *
     * @param x The scaling factor along the X axis.
     * @param y The scaling factor along the Y axis.
     * @param z The scaling factor along the Z axis.
     * @return This matrix after being set to the scaling matrix.
     */
    fun scaling(x: Float, y: Float, z: Float): Matrix4f {
        identity()
        m[0] = x
        m[5] = y
        m[10] = z
        return this
    }


    /**
     * Applies this matrix to a vector and returns the transformed vector.
     *
     * @param v The vector to be transformed.
     * @return The transformed vector.
     */
    fun transform(v: Vec3f): Vec3f {
        val x = v.x * m[0] + v.y * m[4] + v.z * m[8] + m[12]
        val y = v.x * m[1] + v.y * m[5] + v.z * m[9] + m[13]
        val z = v.x * m[2] + v.y * m[6] + v.z * m[10] + m[14]
        return Vec3f(x, y, z)
    }

    /**
     * Sets this matrix to a combined translation, rotation (using a quaternion), and scaling matrix.
     *
     * @param translation The translation vector.
     * @param rotation The rotation represented as a quaternion.
     * @param scale The scaling factor.
     * @return This matrix after being set to the combined translation, rotation, and scaling matrix.
     */
    fun translationRotateScale(translation: Vec3f, rotation: Quaternionf, scale: Float): Matrix4f {
        val xx = rotation.x * rotation.x
        val xy = rotation.x * rotation.y
        val xz = rotation.x * rotation.z
        val xw = rotation.x * rotation.w
        val yy = rotation.y * rotation.y
        val yz = rotation.y * rotation.z
        val yw = rotation.y * rotation.w
        val zz = rotation.z * rotation.z
        val zw = rotation.z * rotation.w

        m[0] = scale * (1.0f - 2.0f * (yy + zz))
        m[1] = scale * (2.0f * (xy + zw))
        m[2] = scale * (2.0f * (xz - yw))
        m[3] = 0.0f

        m[4] = scale * (2.0f * (xy - zw))
        m[5] = scale * (1.0f - 2.0f * (xx + zz))
        m[6] = scale * (2.0f * (yz + xw))
        m[7] = 0.0f

        m[8] = scale * (2.0f * (xz + yw))
        m[9] = scale * (2.0f * (yz - xw))
        m[10] = scale * (1.0f - 2.0f * (xx + yy))
        m[11] = 0.0f

        m[12] = translation.x
        m[13] = translation.y
        m[14] = translation.z
        m[15] = 1.0f

        return this
    }

    /**
     * Provides a string representation of the matrix.
     *
     * @return A string representation of the matrix in a readable format.
     */
    override fun toString(): String {
        return """
             [${m[0]}, ${m[4]}, ${m[8]}, ${m[12]}]
             [${m[1]}, ${m[5]}, ${m[9]}, ${m[13]}]
             [${m[2]}, ${m[6]}, ${m[10]}, ${m[14]}]
             [${m[3]}, ${m[7]}, ${m[11]}, ${m[15]}]
             """.trimIndent()
    }

    /**
     * Copies the matrix components into a FloatBuffer.
     *
     * @param buffer The FloatBuffer to copy the matrix data into.
     * @return The FloatBuffer with the matrix data.
     */
    operator fun get(buffer: FloatBuffer): FloatBuffer {
        buffer.clear() // Clear the buffer before putting data
        buffer.put(m)  // Put the matrix data into the buffer
        buffer.flip()  // Flip the buffer to prepare it for reading
        return buffer
    }

    /**
     * Creates a FloatBuffer from this matrix's data.
     *
     * @return A FloatBuffer containing the matrix data.
     */
    fun getFloatBuffer(): FloatBuffer {
        // Allocate a direct ByteBuffer with capacity for the float array
        val byteBuffer = ByteBuffer.allocateDirect(m.size * 4).order(ByteOrder.nativeOrder())

        // Convert the ByteBuffer into a FloatBuffer
        val floatBuffer = byteBuffer.asFloatBuffer()

        // Put the float array into the FloatBuffer
        floatBuffer.put(m)

        // Prepare the buffer for reading (flip it)
        floatBuffer.flip()

        return floatBuffer
    }
}
