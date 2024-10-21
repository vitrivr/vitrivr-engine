package org.vitrivr.engine.model3d.lwjglrender.util.math

import kotlin.math.sqrt


object MathConstants {
    /**
     * Definition of the golden ratio PHI.
     */
    val PHI: Double = ((1.0 + sqrt(5.0)) / 2.0)

    /**
     * Square-root of three.
     */
    val SQRT3: Double = sqrt(3.0)

    /**
     * Square-root of two.
     */
    val SQRT2: Double = sqrt(2.0)

    /**
     * Square-root of two.
     */
    val SQRT1_5: Double = sqrt(1.5)


    /**
     * Defines the vertices of a regular Cube.
     */
    val VERTICES_3D_CUBE: Array<DoubleArray> = arrayOf(
        doubleArrayOf(1.0, 1.0, 1.0),
        doubleArrayOf(-1.0, -1.0, -1.0),
        doubleArrayOf(1.0, -1.0, -1.0),
        doubleArrayOf(-1.0, -1.0, 1.0),
        doubleArrayOf(-1.0, 1.0, -1.0),
        doubleArrayOf(-1.0, 1.0, 1.0),
        doubleArrayOf(1.0, -1.0, 1.0),
        doubleArrayOf(1.0, 1.0, -1.0)
    )

    val VERTICES_3D_3TRIANGLES: Array<DoubleArray> = arrayOf(
        doubleArrayOf(0.0, 0.0, SQRT3), doubleArrayOf(SQRT1_5, 0.0, -SQRT1_5), doubleArrayOf(-SQRT1_5, 0.0, -SQRT1_5),
        doubleArrayOf(-1.0, 1.0, 1.0), doubleArrayOf(-1.0, 1.0, -1.0), doubleArrayOf(-SQRT1_5, SQRT1_5, 0.0),
        doubleArrayOf(1.0, -1.0, 1.0), doubleArrayOf(-SQRT1_5, -SQRT1_5, 0.0), doubleArrayOf(1.0, -1.0, -1.0),
    )


    /**
     * Defines the vertices of a regular Dodecahedron.
     */
    val VERTICES_3D_DODECAHEDRON: Array<DoubleArray> = arrayOf(
        doubleArrayOf(1.0, 1.0, 1.0),
        doubleArrayOf(-1.0, -1.0, -1.0),
        doubleArrayOf(1.0, -1.0, -1.0),
        doubleArrayOf(-1.0, -1.0, 1.0),
        doubleArrayOf(-1.0, 1.0, -1.0),
        doubleArrayOf(-1.0, 1.0, 1.0),
        doubleArrayOf(1.0, -1.0, 1.0),
        doubleArrayOf(1.0, 1.0, -1.0),
        doubleArrayOf(0.0, 1 / PHI, PHI),
        doubleArrayOf(0.0, -1 / PHI, PHI),
        doubleArrayOf(0.0, 1 / PHI, -PHI),
        doubleArrayOf(0.0, -1 / PHI, -PHI),
        doubleArrayOf(1 / PHI, PHI, 0.0),
        doubleArrayOf(-1 / PHI, PHI, 0.0),
        doubleArrayOf(1 / PHI, -PHI, 0.0),
        doubleArrayOf(-1 / PHI, -PHI, 0.0),
        doubleArrayOf(PHI, 0.0, 1 / PHI),
        doubleArrayOf(-PHI, 0.0, 1 / PHI),
        doubleArrayOf(PHI, 0.0, -1 / PHI),
        doubleArrayOf(-PHI, 0.0, -1 / PHI)
    )
}