package org.vitrivr.engine.model3d.lwjglrender.util.texturemodel.entroopyoptimizer

import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.joml.Vector3f
import org.vitrivr.engine.core.model.mesh.texturemodel.IModel
import org.vitrivr.engine.core.model.mesh.texturemodel.util.entropyoptimizer.EntopyCalculationMethod
import org.vitrivr.engine.core.model.mesh.texturemodel.util.entropyoptimizer.EntropyOptimizerStrategy
import org.vitrivr.engine.core.model.mesh.texturemodel.util.entropyoptimizer.OptimizerOptions
import java.util.stream.IntStream
import kotlin.math.abs
import kotlin.math.ln
import kotlin.math.log10
import kotlin.math.max

/**
 * Static class for optimizing the view vector of a model to maximize the viewpoint entropy on the model.
 */
object ModelEntropyOptimizer {
    private val LOGGER: Logger = LogManager.getLogger()

    /**
     * Calculates the view vector with the maximum entropy of the model. Uses standard Options for optimizer and entropy calculation.
     *
     * @param model Model to calculate the view vector for.
     * @return View vector with the maximum entropy.
     */
    fun getViewVectorWithMaximizedEntropy(model: IModel): Vector3f {
        return getViewVectorWithMaximizedEntropy(model, OptimizerOptions())
    }

    /**
     * Calculates the view vector with the maximum entropy of the model.
     *
     * @param model   Model to calculate the view vector for.
     * @param options Options for the optimizer and entropy calculation.
     * @return View vector with the maximum entropy.
     */
    fun getViewVectorWithMaximizedEntropy(model: IModel, options: OptimizerOptions): Vector3f {
        val normals = model.getAllNormals()
        val viewVector = options.initialViewVector
        val maxEntropyViewVector = optimize(options, normals, viewVector)
        return maxEntropyViewVector
    }

    /**
     * Wrapper for the optimizer strategy. Optimizes the view vector for the given model with the chosen EntropyOptimizer Strategy.
     *
     * @param options    Options for the optimizer and entropy calculation.
     * @param normals    List of normals of the model.
     * @param viewVector Initial view vector.
     * @return Optimized view vector.
     */
    private fun optimize(options: OptimizerOptions, normals: List<Vector3f>, viewVector: Vector3f): Vector3f {
        val optimizer = options.optimizer
        return when (optimizer) {
            EntropyOptimizerStrategy.RANDOMIZED -> {
                optimizeRandomized(options, normals, viewVector)
            }

            EntropyOptimizerStrategy.NEIGHBORHOOD -> {
                optimizeNeighborhood(options, normals, viewVector)
            }

            else -> {
                Vector3f(0f, 0f, 1f)
            }
        }
    }

    /**
     * Optimizes the view vector for the given model with the randomized EntropyOptimizer Strategy.
     *
     * @param options    Options for the optimizer and entropy calculation.
     *
     *  *  Uses the option iterations. For each iteration a random view vector is generated.
     *  *  Uses the option zoomOutFactor. The view vector is zoomed out by this factor.
     *
     * @param normals    List of normals of the model.
     * @param viewVector Initial view vector.
     * @return Optimized view vector.
     */
    private fun optimizeRandomized(options: OptimizerOptions, normals: List<Vector3f>, viewVector: Vector3f): Vector3f {
        val t0 = System.currentTimeMillis()
        val iterations = options.iterations
        var maxEntropy = calculateEntropy(options, normals, viewVector)
        var maxEntropyViewVector = viewVector
        var ic = 0
        ic = 0
        while (ic < iterations) {
            val randomViewVector = Vector3f(
                (Math.random() - 0.5).toFloat() * 2f, (Math.random() - 0.5).toFloat() * 2f,
                (Math.random() - 0.5).toFloat() * 2f
            )
            randomViewVector.normalize()
            // For Entropy calculation benchmarking comment out the following lines. (Logger is slow)
            // var t0_0 = System.nanoTime();
            val entropy = calculateEntropy(options, normals, randomViewVector)
            // var t1_0 = System.nanoTime();
            // LOGGER.trace("Entropy: {} for ViewVector: {} took {} ns", entropy, randomViewVector, t1_0 - t0_0);
            if (entropy > maxEntropy) {
                maxEntropy = entropy
                maxEntropyViewVector = randomViewVector
            }
            ic++
        }
        val t1 = System.currentTimeMillis()
        LOGGER.trace(
            "Optimization took {} ms with {} iterations for {} normals, getting a max. Entropy of {}. Resulting in {} us/normal",
            t1 - t0, ic + 1, normals.size, maxEntropy, (t1 - t0) * 1000L / normals.size.toLong()
        )
        return maxEntropyViewVector.mul(options.zoomOutFactor)
    }

    /**
     * Optimizes the view vector for the given model with the neighborhood EntropyOptimizer Strategy.
     *
     * @param options    Options for the optimizer and entropy calculation.
     * @param normals    List of normals of the model.
     * @param viewVector Initial view vector.
     * @return Optimized view vector.
     */
    private fun optimizeNeighborhood(
        options: OptimizerOptions,
        normals: List<Vector3f>,
        viewVector: Vector3f
    ): Vector3f {
        return Vector3f(0f, 0f, 1f)
    }

    /**
     * Wrapper for the entropy calculation strategy. Calculates the entropy of the model for the given view vector.
     *
     * @param options    Options for the optimizer and entropy calculation.
     * @param normals    List of normals of the model.
     * @param viewVector View vector.
     * @return Entropy of the model for the given view vector.
     */
    private fun calculateEntropy(options: OptimizerOptions, normals: List<Vector3f>, viewVector: Vector3f): Float {
        val method = options.method
        return when (method) {
            EntopyCalculationMethod.RELATIVE_TO_TOTAL_AREA -> {
                calculateEntropyRelativeToTotalArea(normals, viewVector)
            }

            EntopyCalculationMethod.RELATIVE_TO_TOTAL_AREA_WEIGHTED -> {
                calculateEntropyRelativeToTotalAreaWeighted(normals, viewVector, options)
            }

            EntopyCalculationMethod.RELATIVE_TO_PROJECTED_AREA -> {
                calculateEntropyRelativeToProjectedArea(normals, viewVector)
            }

            else -> {
                0f
            }
        }
    }

    /**
     * Calculates the entropy of the model for the given view vector relative to the projected area of the model.
     *
     * @param normals    List of normals of the model.
     * @param viewVector View vector.
     * @return Entropy of the model for the given view vector.
     */
    private fun calculateEntropyRelativeToProjectedArea(normals: List<Vector3f>, viewVector: Vector3f): Float {
        return 0f
    }

    /**
     * Calculates the entropy of the model for the given view vector relative to the projected area of the model. see: [Google Scholar](https://scholar.google.ch/scholar?hl=de&as_sdt=0%2C5&as_vis=1&q=Viewpoint+selection+using+viewpoint+entrop&btnG=) see: <a> href="https://citeseerx.ist.psu.edu/document?repid=rep1&type=pdf&doi=b854422671e5469373fd49fb3a916910b49a6920">Paper</a>
     *
     * @param normals    List of normals of the model.
     * @param viewVector View vector.
     * @return Entropy of the model for the given view vector.
     */
    private fun calculateEntropyRelativeToTotalArea_old(normals: List<Vector3f>, viewVector: Vector3f): Float {
        val areas = java.util.ArrayList<Float>(normals.size)
        val projected = java.util.ArrayList<Float>(normals.size)
        normals.stream().map { normal: Vector3f? ->
            viewVector.dot(
                normal
            ) / 2f
        }.forEach { e: Float -> areas.add(e) }
        areas.stream().map { area: Float -> if (area > 0f) area else 0f }.forEach { e: Float ->
            projected.add(
                e
            )
        }
        val totalArea = areas.stream().map { a: Float? ->
            abs(
                a!!
            )
        }.reduce(
            0f
        ) { a: Float, b: Float -> java.lang.Float.sum(a, b) }
        val relativeProjected = projected.stream().map { x: Float -> x / totalArea }.toList()
        val logRelativeProjected = relativeProjected.stream().map { x: Float? ->
            log2(
                x!!
            )
        }.toList()
        assert(relativeProjected.size == logRelativeProjected.size)
        val result = IntStream.range(0, relativeProjected.size)
            .mapToObj { ic: Int ->
                relativeProjected[ic] * logRelativeProjected[ic]
            }
            .reduce(0f) { a: Float, b: Float -> java.lang.Float.sum(a, b) }
        val entropy = -result
        return entropy
    }

    /**
     * Calculates the entropy of the model for the given view vector relative to the projected area of the model. see: [Google Scholar](https://scholar.google.ch/scholar?hl=de&as_sdt=0%2C5&as_vis=1&q=Viewpoint+selection+using+viewpoint+entrop&btnG=) see: <a> href="https://citeseerx.ist.psu.edu/document?repid=rep1&type=pdf&doi=b854422671e5469373fd49fb3a916910b49a6920">Paper</a>
     *
     * @param normals    List of normals of the model.
     * @param viewVector View vector.
     * @return Entropy of the model for the given view vector.
     */
    private fun calculateEntropyRelativeToTotalArea2(normals: List<Vector3f>, viewVector: Vector3f): Float {
        var areas: FloatArray? = FloatArray(normals.size)
        val projected = FloatArray(normals.size)
        var totalArea = 0f
        for (ic in normals.indices) {
            areas!![ic] = viewVector.dot(normals[ic]) / 2f
            projected[ic] = max(areas[ic].toDouble(), 0.0).toFloat()
            totalArea += abs(areas[ic].toDouble()).toFloat()
        }
        areas = null
        var entropy = 0f
        for (ic in normals.indices) {
            projected[ic] /= totalArea
            entropy += projected[ic] * log2(
                projected[ic]
            )
        }

        return -entropy
    }

    /**
     * Calculates the entropy of the model for the given view vector relative to the projected area of the model. see: [Google Scholar](https://scholar.google.ch/scholar?hl=de&as_sdt=0%2C5&as_vis=1&q=Viewpoint+selection+using+viewpoint+entrop&btnG=) see: <a> href="https://citeseerx.ist.psu.edu/document?repid=rep1&type=pdf&doi=b854422671e5469373fd49fb3a916910b49a6920">Paper</a>
     *
     * @param normals    List of normals of the model.
     * @param viewVector View vector.
     * @param opts       Optimizer options.
     * @return Entropy of the model for the given view vector.
     */
    private fun calculateEntropyRelativeToTotalAreaWeighted(
        normals: List<Vector3f>,
        viewVector: Vector3f,
        opts: OptimizerOptions
    ): Float {
        val weightedNormals = ArrayList<Vector3f>(normals.size)
        normals.stream().parallel().forEach { n: Vector3f ->
            if (n.y > 0) {
                weightedNormals.add(Vector3f(n.x, n.y * opts.yPosWeight, n.z))
            } else {
                weightedNormals.add(Vector3f(n.x, n.y * opts.yNegWeight, n.z))
            }
        }
        return calculateEntropyRelativeToTotalArea(weightedNormals, viewVector)
    }

    /**
     * Calculates the entropy of the model for the given view vector relative to the projected area of the model. see: [Google Scholar](https://scholar.google.ch/scholar?hl=de&as_sdt=0%2C5&as_vis=1&q=Viewpoint+selection+using+viewpoint+entrop&btnG=) see: <a> href="https://citeseerx.ist.psu.edu/document?repid=rep1&type=pdf&doi=b854422671e5469373fd49fb3a916910b49a6920">Paper</a>
     *
     * @param normals    List of normals of the model.
     * @param viewVector View vector.
     * @return Entropy of the model for the given view vector.
     */
    private fun calculateEntropyRelativeToTotalArea(normals: List<Vector3f>, viewVector: Vector3f): Float {
        val areas = FloatArray(normals.size)
        val projected = FloatArray(normals.size)
        var totalArea = 0f
        IntStream.range(0, normals.size).parallel().forEach { ic: Int ->
            areas[ic] = viewVector.dot(normals[ic])
            projected[ic] = max(areas[ic].toDouble(), 0.0).toFloat()
            areas[ic] = abs(areas[ic].toDouble()).toFloat()
        }

        for (ic in normals.indices) {
            totalArea += normals[ic].length()
        }

        var entropy = 0f
        val finalTotalArea = totalArea
        IntStream.range(0, normals.size).parallel().forEach { ic: Int ->
            projected[ic] /= finalTotalArea
            projected[ic] =
                projected[ic] * log2(
                    projected[ic]
                )
        }

        for (ic in normals.indices) {
            entropy += projected[ic]
        }

        return -entropy
    }


    /**
     * Static values for log base 2, due to performance reasons.
     */
    private val LOG10OF2 = log10(2.0).toFloat()

    /**
     * Calculates the logarithm of a number to base 2. If x is 0, 0 is returned.
     *
     * @param x The number to calculate the logarithm for.
     * @return log2(x)
     */
    private fun log2(x: Float): Float {
        if (x <= 0f) {
            return 0f
        }
        return (ln(x.toDouble()).toFloat() / LOG10OF2)
    }
}