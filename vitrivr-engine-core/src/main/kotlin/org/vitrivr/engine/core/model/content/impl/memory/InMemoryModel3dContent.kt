package org.vitrivr.engine.core.model.content.impl.memory

import org.vitrivr.engine.core.model.content.element.ContentId
import org.vitrivr.engine.core.model.content.element.ImageContent
import org.vitrivr.engine.core.model.content.element.Model3dContent
import org.vitrivr.engine.core.model.mesh.texturemodel.Model3d

/**
 * A naive in-memory implementation of the [ImageContent] interface.
 *
 * Warning: Usage of [InMemoryModel3dContent] may lead to out-of-memory situations in large extraction pipelines.
 *
 * @author Luca Rossetto.
 * @version 1.0.0
 */
data class InMemoryModel3dContent(override val content: Model3d, override val id: ContentId = ContentId.randomUUID()) : Model3dContent
