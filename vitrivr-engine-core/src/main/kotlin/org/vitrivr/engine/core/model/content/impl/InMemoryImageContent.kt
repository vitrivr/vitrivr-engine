package org.vitrivr.engine.core.model.content.impl

import org.vitrivr.engine.core.model.content.SourcedContent
import org.vitrivr.engine.core.model.content.ImageContent
import org.vitrivr.engine.core.source.Source
import java.awt.image.BufferedImage

/**
 * A naive in-memory implementation of the [ImageContent] interface.
 *
 * Warning: Usage of [InMemoryImageContent] may lead to out-of-memory situations in large extraction pipelines.
 *
 * @author Luca Rossetto.
 * @version 1.0.0
 */
@JvmRecord
data class InMemoryImageContent(override val source: Source, override val image: BufferedImage) : ImageContent, SourcedContent
