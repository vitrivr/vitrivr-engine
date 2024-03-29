package org.vitrivr.engine.core.model.content.impl

import org.vitrivr.engine.core.model.content.element.ImageContent
import org.vitrivr.engine.core.model.content.element.Model3DContent
import org.vitrivr.engine.core.model.mesh.Model3D
import java.awt.image.BufferedImage

/**
 * A naive in-memory implementation of the [ImageContent] interface.
 *
 * Warning: Usage of [InMemoryMeshContent] may lead to out-of-memory situations in large extraction pipelines.
 *
 * @author Luca Rossetto.
 * @version 1.0.0
 */
data class InMemoryMeshContent(override val content: Model3D) : Model3DContent
