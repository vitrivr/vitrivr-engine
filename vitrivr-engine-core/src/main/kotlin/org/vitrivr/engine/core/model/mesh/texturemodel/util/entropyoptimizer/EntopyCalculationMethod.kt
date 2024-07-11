package org.vitrivr.engine.core.model.mesh.texturemodel.util.entropyoptimizer

/**
 * Options for the Entropy calculation.
 */
enum class EntopyCalculationMethod {
    /**
     * The entropy is calculated relative to the total area of the all faces.
     */
    RELATIVE_TO_TOTAL_AREA,

    /**
     * The entropy is calculated relative to the projected area of the faces.
     */
    RELATIVE_TO_PROJECTED_AREA,

    /**
     * The entropy is calculated relative to the projected area of the faces.
     * Additionally, the weight of y component of the faces is taken into account.
     */
    RELATIVE_TO_TOTAL_AREA_WEIGHTED,
}
