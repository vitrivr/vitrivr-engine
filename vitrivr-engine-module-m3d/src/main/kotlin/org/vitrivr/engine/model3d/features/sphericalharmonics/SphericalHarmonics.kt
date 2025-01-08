package org.vitrivr.engine.model3d.features.sphericalharmonics

import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import org.apache.commons.math3.complex.Complex
import org.apache.commons.math3.util.FastMath
import org.vitrivr.engine.core.context.IndexContext
import org.vitrivr.engine.core.context.QueryContext
import org.vitrivr.engine.core.features.dense.DenseRetriever
import org.vitrivr.engine.core.math.correspondence.LinearCorrespondence
import org.vitrivr.engine.core.model.content.element.ContentElement
import org.vitrivr.engine.core.model.content.element.Model3dContent
import org.vitrivr.engine.core.model.descriptor.vector.FloatVectorDescriptor
import org.vitrivr.engine.core.model.mesh.texturemodel.Mesh
import org.vitrivr.engine.core.model.metamodel.Analyser
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.query.Query
import org.vitrivr.engine.core.model.query.basics.Distance
import org.vitrivr.engine.core.model.query.proximity.ProximityQuery
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.model.types.Value
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.core.operators.ingest.Extractor
import org.vitrivr.engine.core.operators.retrieve.Retriever
import org.vitrivr.engine.core.util.math.MathHelper
import org.vitrivr.engine.model3d.model.voxel.VoxelModel
import org.vitrivr.engine.model3d.model.voxel.Voxelizer
import java.util.*
import kotlin.reflect.KClass

private val logger: KLogger = KotlinLogging.logger {}

/**
 * Implementation of the [SphericalHarmonics] [Analyser], which derives leverages spherical harmonics for meshes as proposed in [1].
 *
 * [1] Funkhouser, T., Min, P., Kazhdan, M., Chen, J., Halderman, A., Dobkin, D., & Jacobs, D. (2003).
 *  A search engine for 3D models. ACM Trans. Graph., 22(1), 83–105. http://doi.org/10.1145/588272.588279
 *
 * @author Ralph Gasser
 * @version 1.2.0
 */
class SphericalHarmonics : Analyser<Model3dContent, FloatVectorDescriptor> {
    companion object {
        /** Name of the grid_size parameter; determines size of voxel grid for rasterization. */
        const val GRID_SIZE_PARAMETER_NAME = "grid_size"

        /** Default value of the grid_size parameter. */
        const val GRID_SIZE_PARAMETER_DEFAULT = 64

        /** Name of the cap parameter; determines maximum radius to obtain spherical harmonics for. */
        const val CAP_PARAMETER_NAME = "cap"

        /** Default value of the cap parameter. */
        const val CAP_PARAMETER_DEFAULT = 10

        /** Name of the min_l parameter; determines minimum l to obtain spherical harmonics for. */
        const val MINL_PARAMETER_NAME = "min_l"

        /**  Default value of the min_l parameter. */
        const val MINL_PARAMETER_DEFAULT = 0

        /** Name of the max_l parameter; determines maximum l to obtain spherical harmonics for. */
        const val MAXL_PARAMETER_NAME = "max_l"

        /**  Default value of the max_l parameter. */
        const val MAXL_PARAMETER_DEFAULT = 4

        /**
         * Obtains the [SphericalHarmonics] descriptor of the [Mesh].
         *
         * To do so, the Mesh is rasterized into a [VoxelModel] of gridSize x gridSize x gridSize Voxels (one *safety-voxel*
         * per dimension to prevent ArrayIndexOutOfBounds exceptions). This [VoxelModel] is treated as a
         *
         * function f(x,y,z) = 1.0 if Voxel is visible and 0.0 otherwise.
         *
         * The [VoxelModel] is sampled at 7 different radii r ranging from 0.25 to 1.0, where 0.0 lies at the center of
         * the grid and 1.0 touches the bounding-box of the grid. The parameters r, ϑ, ϼ relate to the VoxelGrid as follows:
         *
         * f(x,y,z) = f(r * sin(ϑ) * cos(ϼ) * α + α, r * cos(ϑ) * α, + α, r * sin(ϑ) * sin(ϼ) * α + α)
         *
         * Where α is a constant used to translate the normalized coordinate system to the bounds of the VoxelGrid.
         *
         * Now for l = 0 to l = 4 (m = -l to +l), the projection of the function f(x,y,z) onto the SphericalHarmonic function Zlm (i.e. the integral  ∫f(ϑ,ϼ)Zlm(ϑ,ϼ)dϴdϑ) is calculated. This is done for all of the seven radii. This yields 25 descriptors per radius which results in a feature vector of 7 * 25 entries.
         *
         * Depending on the model, the first components may be 0.0 because the surface of the sphere defined by the radius only touches empty space (i.e the hollow interior of the model).
         *
         * @param gridSize The grid size for rasterization of [Mesh]
         * @param cap The maximum radius to obtain [SphericalHarmonics] function value for.
         * @param minL The minimum L parameter to obtain [SphericalHarmonics] function value for.
         * @param maxL The maximum L parameter to obtain [SphericalHarmonics] function value for.
         */
        fun analyse(mesh: Mesh, gridSize: Int, cap: Int, minL: Int, maxL: Int): FloatVectorDescriptor {
            val voxelizer = Voxelizer(2.0f / gridSize)
            val increment = 0.1 /* Increment of the angles during calculation of the descriptors. */
            val R: Int = gridSize / 2
            val numberOfCoefficients: Int = SphericalHarmonicsFunction.numberOfCoefficients(maxL, true) - SphericalHarmonicsFunction.numberOfCoefficients(minL - 1, true)

            /* Prepares an empty array for the feature vector. */
            val feature = FloatArray((R - cap) * numberOfCoefficients) { _ -> 0f }

            /* Voxelizes the grid from the mesh. If the resulting grid is invisible, the method returns immediately. */
            val grid: VoxelModel = voxelizer.voxelize(mesh, gridSize + 1, gridSize + 1, gridSize + 1)
            if (!grid.isVisible()) {
                return FloatVectorDescriptor(UUID.randomUUID(), null, Value.FloatVector(feature))
            }

            val descriptors: MutableList<MutableList<Complex>> = LinkedList()

            /* Outer-loops; iterate from l=0 to 5 and m=-l to +l. For each combination, a new SphericalHarmonicsFunction is created. */
            for (l in minL..maxL) {
                for (m in 0..l) {
                    val fkt = SphericalHarmonicsFunction(l, m)
                    /* * Middle-loop; Iterate over the 7 radii. */
                    for (r in 0 until R - cap) {
                        /* Allocate array list for radius. */
                        if (descriptors.size <= r) {
                            descriptors.add(ArrayList<Complex>())
                        }
                        val list: MutableList<Complex> = descriptors[r]

                        var result = Complex(0.0)

                        /* Calculate the projections at radius r for l and m (i.e. the integral ∫f(ϑ,ϼ)Zlm(ϑ,ϼ)dϴdϑ). */
                        var theta = 0.0
                        while (theta <= 2 * Math.PI) {
                            var phi = 0.0
                            while (phi <= Math.PI) {
                                val x = ((r + 1) * FastMath.sin(theta) * FastMath.cos(phi)).toInt() + R
                                val y = ((r + 1) * FastMath.cos(theta)).toInt() + R
                                val z = ((r + 1) * FastMath.sin(theta) * FastMath.sin(phi)).toInt() + R

                                if (grid[x, y, z]) {
                                    result = result.add(fkt.value(theta, phi).conjugate().multiply(increment * increment))
                                }
                                phi += increment
                            }
                            theta += increment
                        }

                        list.add(result)
                    }
                }
            }

            /* Assembles the actual feature vector. */
            var index = 0
            for (radius in descriptors) {
                for (descriptor in radius) {
                    feature[index++] = descriptor.abs().toFloat()
                }
            }

            /* Returns the normalized vector. */
            return FloatVectorDescriptor(UUID.randomUUID(), null, Value.FloatVector(MathHelper.normalizeL2(feature)))
        }
    }

    override val contentClasses: Set<KClass<out ContentElement<*>>> = setOf(Model3dContent::class)
    override val descriptorClass: KClass<FloatVectorDescriptor> = FloatVectorDescriptor::class
    override fun prototype(field: Schema.Field<*, *>): FloatVectorDescriptor {
        val gridSize = field.parameters[GRID_SIZE_PARAMETER_NAME]?.toIntOrNull() ?: GRID_SIZE_PARAMETER_DEFAULT
        val cap = field.parameters[CAP_PARAMETER_NAME]?.toIntOrNull() ?: CAP_PARAMETER_DEFAULT
        val minL = field.parameters[MINL_PARAMETER_NAME]?.toIntOrNull() ?: MINL_PARAMETER_DEFAULT
        val maxL = field.parameters[MAXL_PARAMETER_NAME]?.toIntOrNull() ?: MAXL_PARAMETER_DEFAULT
        val numberOfCoefficients: Int = SphericalHarmonicsFunction.numberOfCoefficients(maxL, true) - SphericalHarmonicsFunction.numberOfCoefficients(minL - 1, true)
        val vectorSize = ((gridSize / 2) - cap) * numberOfCoefficients
        return FloatVectorDescriptor(UUID.randomUUID(), UUID.randomUUID(), Value.FloatVector(FloatArray(vectorSize)))
    }

    override fun newRetrieverForQuery(field: Schema.Field<Model3dContent, FloatVectorDescriptor>, query: Query, context: QueryContext): Retriever<Model3dContent, FloatVectorDescriptor> {
        require(query is ProximityQuery<*> && query.value is Value.FloatVector) { }
        return  DenseRetriever(field, query, context, LinearCorrespondence(1.0f))
    }

    override fun newRetrieverForDescriptors(field: Schema.Field<Model3dContent, FloatVectorDescriptor>, descriptors: Collection<FloatVectorDescriptor>, context: QueryContext): Retriever<Model3dContent, FloatVectorDescriptor> {
        /* Extract parameters from field and context. */
        val k = context.getProperty(field.fieldName, "limit")?.toLongOrNull() ?: 1000L
        val returnDescriptor = context.getProperty(field.fieldName, "returnDescriptor")?.toBooleanStrictOrNull() ?: false

        /* Construct query and return retriever. */
        val query = ProximityQuery(value = descriptors.first().vector, k = k, distance = Distance.EUCLIDEAN, fetchVector = returnDescriptor)
        return this.newRetrieverForQuery(field, query, context)
    }

    override fun newRetrieverForContent(field: Schema.Field<Model3dContent, FloatVectorDescriptor>, content: Collection<Model3dContent>, context: QueryContext): Retriever<Model3dContent, FloatVectorDescriptor> {
        require(field.analyser == this) { "The field '${field.fieldName}' analyser does not correspond with this analyser. This is a programmer's error!" }

        /* Extract parameters from field and context. */
        val gridSize = field.parameters[GRID_SIZE_PARAMETER_NAME]?.toIntOrNull() ?: GRID_SIZE_PARAMETER_DEFAULT
        val cap = field.parameters[CAP_PARAMETER_NAME]?.toIntOrNull() ?: CAP_PARAMETER_DEFAULT
        val minL = field.parameters[MINL_PARAMETER_NAME]?.toIntOrNull() ?: MINL_PARAMETER_DEFAULT
        val maxL = field.parameters[MAXL_PARAMETER_NAME]?.toIntOrNull() ?: MAXL_PARAMETER_DEFAULT
        val descriptors = content.map { analyse(it.content.getMaterials().first().materialMeshes.first(), gridSize, cap, minL, maxL) }

        /* Return retriever. */
        return this.newRetrieverForDescriptors(field, descriptors, context)
    }

    /**
     * Generates and returns a new [SphericalHarmonicsExtractor] instance for this [SphericalHarmonics].
     *
     * @param field The [Schema.Field] to create an [Extractor] for.
     * @param input The [Operator] that acts as input to the new [Extractor].
     * @param context The [IndexContext] to use with the [Extractor].
     * @return [SphericalHarmonicsExtractor]
     */
    override fun newExtractor(field: Schema.Field<Model3dContent, FloatVectorDescriptor>, input: Operator<Retrievable>, context: IndexContext): SphericalHarmonicsExtractor {
        val gridSize = field.parameters[GRID_SIZE_PARAMETER_NAME]?.toIntOrNull() ?: context.getProperty("", "")?.toIntOrNull() ?: GRID_SIZE_PARAMETER_DEFAULT
        val cap = field.parameters[CAP_PARAMETER_NAME]?.toIntOrNull() ?: context.getProperty("", "")?.toIntOrNull() ?: CAP_PARAMETER_DEFAULT
        val minL = field.parameters[MINL_PARAMETER_NAME]?.toIntOrNull() ?: context.getProperty("", "")?.toIntOrNull() ?: MINL_PARAMETER_DEFAULT
        val maxL = field.parameters[MAXL_PARAMETER_NAME]?.toIntOrNull() ?: context.getProperty("", "")?.toIntOrNull() ?: MAXL_PARAMETER_DEFAULT
        logger.debug { "Creating new SphericalHarmonicsExtract for field '${field.fieldName}' with parameters ($gridSize, $cap, $minL, $maxL)." }
        return SphericalHarmonicsExtractor(input, this, field, gridSize, cap, minL, maxL)
    }

    /**
     * Generates and returns a new [SphericalHarmonicsExtractor] instance for this [SphericalHarmonics].
     *
     * @param name The name of the [SphericalHarmonicsExtractor].
     * @param input The [Operator] that acts as input to the new [Extractor].
     * @param context The [IndexContext] to use with the [Extractor].
     * @return [SphericalHarmonicsExtractor]
     */
    override fun newExtractor(name: String, input: Operator<Retrievable>, context: IndexContext): SphericalHarmonicsExtractor {
        val gridSize = context.getProperty(name, GRID_SIZE_PARAMETER_NAME)?.toIntOrNull() ?: GRID_SIZE_PARAMETER_DEFAULT
        val cap = context.getProperty(name, CAP_PARAMETER_NAME)?.toIntOrNull() ?: CAP_PARAMETER_DEFAULT
        val minL = context.getProperty(name, MINL_PARAMETER_NAME)?.toIntOrNull() ?: MINL_PARAMETER_DEFAULT
        val maxL = context.getProperty(name, MAXL_PARAMETER_NAME)?.toIntOrNull() ?: MAXL_PARAMETER_DEFAULT
        logger.debug { "Creating new SphericalHarmonicsExtract with parameters ($gridSize, $cap, $minL, $maxL)." }
        return SphericalHarmonicsExtractor(input, this,  name, gridSize, cap, minL, maxL)
    }
}
