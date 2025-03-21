package org.vitrivr.engine.model3d.features.sphericalharmonics

import org.vitrivr.engine.core.features.AbstractExtractor
import org.vitrivr.engine.core.features.metadata.source.file.FileSourceMetadataExtractor
import org.vitrivr.engine.core.model.content.ContentType
import org.vitrivr.engine.core.model.content.element.Model3dContent
import org.vitrivr.engine.core.model.descriptor.vector.FloatVectorDescriptor
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.core.operators.ingest.Extractor
import org.vitrivr.engine.core.source.file.FileSource

/**
 * Implementation of an [AbstractExtractor], which derives leverages spherical harmonics for meshes as proposed in [1].
 *
 * [1] Funkhouser, T., Min, P., Kazhdan, M., Chen, J., Halderman, A., Dobkin, D., & Jacobs, D. (2003).
 *  A search engine for 3D models. ACM Trans. Graph., 22(1), 83–105. http://doi.org/10.1145/588272.588279
 *
 * @author Ralph Gasser
 * @version 1.2.0
 */
class SphericalHarmonicsExtractor :
    AbstractExtractor<Model3dContent, FloatVectorDescriptor> {

    private val gridSize: Int
    private val cap: Int
    private val minL: Int
    private val maxL: Int

    constructor(
        input: Operator<Retrievable>,
        analyser: SphericalHarmonics,
        field: Schema.Field<Model3dContent, FloatVectorDescriptor>,
        gridSize: Int,
        cap: Int,
        minL: Int,
        maxL: Int
    ) : super(input, analyser, field) {
        this.gridSize = gridSize
        this.cap = cap
        this.minL = minL
        this.maxL = maxL

        require(this.minL < this.maxL) { "Parameter mismatch: min_l must be smaller than max_l. " }

    }

    constructor(
        input: Operator<Retrievable>,
        analyser: SphericalHarmonics,
        name: String,
        gridSize: Int,
        cap: Int,
        minL: Int,
        maxL: Int
    ) : super(input, analyser, name) {
        this.gridSize = gridSize
        this.cap = cap
        this.minL = minL
        this.maxL = maxL

        require(this.minL < this.maxL) { "Parameter mismatch: min_l must be smaller than max_l. " }

    }

    /**
     * Internal method to check, if [Retrievable] matches this [Extractor] and should thus be processed.
     *
     * [FileSourceMetadataExtractor] implementation only works with [RetrievableWithSource] that contain a [FileSource].
     *
     * @param retrievable The [Retrievable] to check.
     * @return True on match, false otherwise,
     */
    override fun matches(retrievable: Retrievable): Boolean = retrievable.content.any { it.type == ContentType.MESH }

    /**
     * Internal method to perform extraction on [Retrievable].
     **
     * @param retrievable The [Retrievable] to process.
     * @return List of resulting [FloatVectorDescriptor]s.
     */
    override fun extract(retrievable: Retrievable) = retrievable.content.filterIsInstance<Model3dContent>().flatMap { c ->
        c.content.getMaterials().flatMap { mat ->
            mat.materialMeshes.map { mesh ->
                SphericalHarmonics.analyse(
                    mesh,
                    this.gridSize,
                    this.minL,
                    this.maxL,
                    this.cap
                ).copy(field = this.field)
            }
        }
    }
}