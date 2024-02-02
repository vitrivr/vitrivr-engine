package org.vitrivr.engine.model3d.features.sphericalharmonics

import org.vitrivr.engine.core.features.AbstractExtractor
import org.vitrivr.engine.core.features.metadata.source.file.FileSourceMetadataExtractor
import org.vitrivr.engine.core.model.content.element.Model3DContent
import org.vitrivr.engine.core.model.descriptor.vector.FloatVectorDescriptor
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.model.retrievable.decorators.RetrievableWithContent
import org.vitrivr.engine.core.model.retrievable.decorators.RetrievableWithSource
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.core.operators.ingest.Extractor
import org.vitrivr.engine.core.source.file.FileSource
import org.vitrivr.engine.model3d.features.sphericalharmonics.SphericalHarmonics.Companion.CAP_PARAMETER_DEFAULT
import org.vitrivr.engine.model3d.features.sphericalharmonics.SphericalHarmonics.Companion.CAP_PARAMETER_NAME
import org.vitrivr.engine.model3d.features.sphericalharmonics.SphericalHarmonics.Companion.GRID_SIZE_PARAMETER_DEFAULT
import org.vitrivr.engine.model3d.features.sphericalharmonics.SphericalHarmonics.Companion.GRID_SIZE_PARAMETER_NAME
import org.vitrivr.engine.model3d.features.sphericalharmonics.SphericalHarmonics.Companion.MAXL_PARAMETER_DEFAULT
import org.vitrivr.engine.model3d.features.sphericalharmonics.SphericalHarmonics.Companion.MAXL_PARAMETER_NAME
import org.vitrivr.engine.model3d.features.sphericalharmonics.SphericalHarmonics.Companion.MINL_PARAMETER_DEFAULT
import org.vitrivr.engine.model3d.features.sphericalharmonics.SphericalHarmonics.Companion.MINL_PARAMETER_NAME

/**
 * Implementation of an [AbstractExtractor], which derives leverages spherical harmonics for meshes as proposed in [1].
 *
 * [1] Funkhouser, T., Min, P., Kazhdan, M., Chen, J., Halderman, A., Dobkin, D., & Jacobs, D. (2003).
 *  A search engine for 3D models. ACM Trans. Graph., 22(1), 83–105. http://doi.org/10.1145/588272.588279
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
class SphericalHarmonicsExtractor(
    input: Operator<Retrievable>,
    field: Schema.Field<Model3DContent, FloatVectorDescriptor>,
    persisting: Boolean = true
) : AbstractExtractor<Model3DContent, FloatVectorDescriptor>(input, field, persisting) {

    /** The grid size for rasterization [SphericalHarmonicsFunction]. */
    private val gridSize = this.field.parameters[GRID_SIZE_PARAMETER_NAME]?.toIntOrNull() ?: GRID_SIZE_PARAMETER_DEFAULT

    /** The maximum radius to obtain [SphericalHarmonicsFunction] values for. */
    private val cap = this.field.parameters[CAP_PARAMETER_NAME]?.toIntOrNull() ?: CAP_PARAMETER_DEFAULT

    /** The maxL parameter used for the [SphericalHarmonicsFunction].. */
    private val minL = this.field.parameters[MINL_PARAMETER_NAME]?.toIntOrNull() ?: MINL_PARAMETER_DEFAULT

    /** The maxL parameter used for the [SphericalHarmonicsFunction]. */
    private val maxL = this.field.parameters[MAXL_PARAMETER_NAME]?.toIntOrNull() ?: MAXL_PARAMETER_DEFAULT

    init {
        require(this.minL < this.maxL) { "Parameter mismatch: min_l must be smaller than max_l. "}
    }

    /**
     * Internal method to check, if [Retrievable] matches this [Extractor] and should thus be processed.
     *
     * [FileSourceMetadataExtractor] implementation only works with [RetrievableWithSource] that contain a [FileSource].
     *
     * @param retrievable The [Retrievable] to check.
     * @return True on match, false otherwise,
     */
    override fun matches(retrievable: Retrievable): Boolean = retrievable is RetrievableWithContent

    /**
     * Internal method to perform extraction on [Retrievable].
     **
     * @param retrievable The [Retrievable] to process.
     * @return List of resulting [FloatVectorDescriptor]s.
     */
    override fun extract(retrievable: Retrievable): List<FloatVectorDescriptor> {
        check(retrievable is RetrievableWithContent) { "Incoming retrievable is not a retrievable with source. This is a programmer's error!" }
        val content = retrievable.content.filterIsInstance<Model3DContent>()
        val analyser = (this.field.analyser as SphericalHarmonics)
        return content.flatMap { c -> c.content.getMaterials().flatMap { mat -> mat.meshes.map { mesh -> analyser.analyse(mesh, this.gridSize, this.minL, this.maxL, this.cap) } } }
    }
}