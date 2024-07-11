package org.vitrivr.engine.model3d.data.util.texturemodel.viewpoint;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joml.Vector3f;
import org.vitrivr.engine.core.model.mesh.texturemodel.IModel;
import org.vitrivr.engine.core.model.mesh.texturemodel.util.entropyoptimizer.EntopyCalculationMethod;
import org.vitrivr.engine.core.model.mesh.texturemodel.util.entropyoptimizer.EntropyOptimizerStrategy;
import org.vitrivr.engine.model3d.data.util.texturemodel.entropyoptimizer.ModelEntropyOptimizer;
import org.vitrivr.engine.core.model.mesh.texturemodel.util.entropyoptimizer.OptimizerOptions;
import org.vitrivr.engine.model3d.data.util.math.MathConstants;

import java.util.LinkedList;
import java.util.stream.IntStream;

public class ViewpointHelper {
    private static final Logger LOGGER = LogManager.getLogger();

    // Zoom factor for the camera
    private static final float ZOOM = 1f;

    /*
     * Helper method returns a list of camera positions for a given model and strategy
     * This method can be simplified once a good strategy is found
     * Or the method can be refactored to ModelEntropyOptimizer
     * @param viewpointStrategy the strategy to use the camera positions
     * @return an array of camera positions
     */
    public static double[][] getCameraPositions(ViewpointStrategy viewpointStrategy, IModel model) {
        var viewVectors = new LinkedList<Vector3f>();
        switch (viewpointStrategy) {
            case RANDOM -> {
                viewVectors.add(new Vector3f(
                        (float) (Math.random() - 0.5) * 2f,
                        (float) (Math.random() - 0.5) * 2f,
                        (float) (Math.random() - 0.5) * 2f)
                        .normalize().mul(ZOOM)
                );
            }
            case UPPER_LEFT -> {
                viewVectors.add(new Vector3f(
                        -1f,
                        1f,
                        1f)
                        .normalize().mul(ZOOM)
                );
            }
            case VIEWPOINT_ENTROPY_MAXIMIZATION_RANDOMIZED_WEIGHTED -> {
                var opts = new OptimizerOptions() {{
                    this.setIterations(100);
                    this.setInitialViewVector(new Vector3f(0, 0, 1));
                    this.setYPosWeight(0.8f);
                    this.setYNegWeight(0.7f);
                    this.setMethod(EntopyCalculationMethod.RELATIVE_TO_TOTAL_AREA_WEIGHTED);
                    this.setOptimizer(EntropyOptimizerStrategy.RANDOMIZED);
                }};
                viewVectors.add(ModelEntropyOptimizer.getViewVectorWithMaximizedEntropy(model, opts));
            }
            case VIEWPOINT_ENTROPY_MAXIMIZATION_RANDOMIZED -> {
                var opts = new OptimizerOptions() {{
                    this.setIterations(100);
                    this.setInitialViewVector(new Vector3f(0, 0, 1));
                    this.setMethod(EntopyCalculationMethod.RELATIVE_TO_TOTAL_AREA);
                    this.setOptimizer(EntropyOptimizerStrategy.RANDOMIZED);
                }};
                viewVectors.add(ModelEntropyOptimizer.getViewVectorWithMaximizedEntropy(model, opts));
            }
            case MULTI_IMAGE_KMEANS, MULTI_IMAGE_FRAME, MULTI_IMAGE_PROJECTEDMEAN -> {
                var views = MathConstants.VERTICES_3D_DODECAHEDRON;
                for (var view : views) {
                    viewVectors.add(new Vector3f(
                            (float) view[0],
                            (float) view[1],
                            (float) view[2])
                            .normalize().mul(ZOOM)
                    );
                }
            }
            // Front and default
            case FRONT -> {
                viewVectors.add(new Vector3f(
                        0f,
                        0f,
                        1)
                        .normalize().mul(ZOOM)
                );
            }
            case MULTI_IMAGE_2_2 -> {
                viewVectors.add(new Vector3f(
                        0f,
                        0f,
                        1)
                        .normalize().mul(ZOOM)
                );
                viewVectors.add(new Vector3f(
                        -1f,
                        1f,
                        1)
                        .normalize().mul(ZOOM)
                );
                var opts1 = new OptimizerOptions() {{
                    this.setIterations(100);
                    this.setInitialViewVector(new Vector3f(0, 0, 1));
                    this.setMethod(EntopyCalculationMethod.RELATIVE_TO_TOTAL_AREA);
                    this.setOptimizer(EntropyOptimizerStrategy.RANDOMIZED);
                }};
                viewVectors.add(ModelEntropyOptimizer.getViewVectorWithMaximizedEntropy(model, opts1));
                var opts2 = new OptimizerOptions() {{
                    this.setIterations(100);
                    this.setInitialViewVector(new Vector3f(0, 0, 1));
                    this.setMethod(EntopyCalculationMethod.RELATIVE_TO_TOTAL_AREA_WEIGHTED);
                    this.setOptimizer(EntropyOptimizerStrategy.RANDOMIZED);
                }};
                viewVectors.add(ModelEntropyOptimizer.getViewVectorWithMaximizedEntropy(model, opts2));
            }
        }
        var camerapositions = new double[viewVectors.size()][3];
        IntStream.range(0, viewVectors.size()).parallel().forEach(ic ->
                {
                    var viewVector = viewVectors.get(ic);
                    camerapositions[ic][0] = viewVector.x;
                    camerapositions[ic][1] = viewVector.y;
                    camerapositions[ic][2] = viewVector.z;
                }
        );
        LOGGER.debug("Camera {} with strategy {}", camerapositions, viewpointStrategy);
        return camerapositions;
    }
}
