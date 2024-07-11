package org.vitrivr.engine.core.model.mesh.texturemodel.util.entropyoptimizer

/**
 * The method used to calculate the entropy.
 */
enum class EntropyOptimizerStrategy {
    /**
     * The new view vector is chosen randomly.
     */
    RANDOMIZED,

    /**
     * The new view vector is chosen by the gradient of the entropy.
     */
    NEIGHBORHOOD,
}
