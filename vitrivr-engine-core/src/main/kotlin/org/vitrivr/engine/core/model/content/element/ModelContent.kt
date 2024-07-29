package org.vitrivr.engine.core.model.content.element

import org.vitrivr.engine.core.model.content.ContentType
import org.vitrivr.engine.core.model.mesh.texturemodel.Model3d

/**
 * A 3D [ContentElement].
 *
 * @author Rahel Arnold
 * @version 1.0.0
 */
interface ModelContent: ContentElement<Model3d>{
    /** The [ContentType] of a [ModelContent] is always [ContentType.MESH]. */

    val id: String
        get() = this.content.modelId

    override val type: ContentType
        get() = ContentType.MESH
}