package org.vitrivr.engine.core.model.content.element

import org.vitrivr.engine.core.model.content.ContentType
import org.vitrivr.engine.core.model.mesh.Model3D

/**
 * A 3D [ContentElement].
 *
 * @author Rahel Arnold
 * @version 1.0.0
 */
interface Model3DContent: ContentElement<Model3D>{
    /** The [ContentType] of a [Model3DContent] is always [ContentType.MESH]. */

    override val type: ContentType
        get() = ContentType.MESH
}