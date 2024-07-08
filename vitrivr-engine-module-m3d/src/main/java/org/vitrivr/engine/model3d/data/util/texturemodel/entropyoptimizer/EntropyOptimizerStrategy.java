package org.vitrivr.engine.model3d.data.util.texturemodel.entropyoptimizer;

/**
 * The method used to calculate the entropy.
 */
public enum EntropyOptimizerStrategy {
  /**
   * The new view vector is chosen randomly.
   */
  RANDOMIZED,
  /**
   * The new view vector is chosen by the gradient of the entropy.
   */
  NEIGHBORHOOD,
}
