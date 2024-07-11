package org.vitrivr.engine.core.model.mesh.texturemodel.util.entropyoptimizer

import org.joml.Vector3f
import org.vitrivr.engine.core.model.mesh.texturemodel.util.entropyoptimizer.EntopyCalculationMethod
import org.vitrivr.engine.core.model.mesh.texturemodel.util.entropyoptimizer.EntropyOptimizerStrategy


/**
 * Options for the ModelEntropyOptimizer and entropy calculation.
 */
open class OptimizerOptions {
    /**
     * The factor the unit vector is multiplied with to zoom.
     *
     *
     * > 1 zooms out, < 1 zooms in.
     */
    var zoomOutFactor: Float = 1f //(float) Math.sqrt(3.0);

    /**
     * The method used to optimize the entropy.
     */
    var optimizer: EntropyOptimizerStrategy = EntropyOptimizerStrategy.RANDOMIZED

    /**
     * The method used to calculate the entropy.
     */
    var method: EntopyCalculationMethod = EntopyCalculationMethod.RELATIVE_TO_TOTAL_AREA

    /**
     * The maximum number of iterations the optimizer should perform.
     */
    var iterations: Int = 1000

    /**
     * The initial view vector.
     */
    var initialViewVector: Vector3f = Vector3f(0f, 0f, 1f)

    /**
     * Weight for y normal vectors pointing up.
     */
    var yPosWeight: Float = 1f

    /**
     * Weight for y normal vectors pointing down.
     */
    var yNegWeight: Float = 1f
}
