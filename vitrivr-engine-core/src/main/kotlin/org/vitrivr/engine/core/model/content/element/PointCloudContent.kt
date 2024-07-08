package org.vitrivr.engine.core.model.content.element

import org.vitrivr.engine.core.model.content.ContentType
import org.vitrivr.engine.core.model.mesh.Model3D
import java.awt.image.BufferedImage

/**
 * A 3D [ContentElement].
 *
 * @author Rahel Arnold
 * @version 1.0.0
 */
interface PointCloudContent: ContentElement<Model3D>{
    /** The [ContentType] of a [PointCloudContent] is always [ContentType.POINT_CLOUD]. */

    val id: String
        get() = this.content.id

    override val type: ContentType
        get() = ContentType.POINT_CLOUD
}