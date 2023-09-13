package org.vitrivr.engine.core.model.content.impl

import org.vitrivr.engine.core.model.content.DerivedContent
import org.vitrivr.engine.core.model.content.ImageContent
import org.vitrivr.engine.core.operators.derive.DerivateName
import java.awt.image.BufferedImage


data class DerivedImageContent(private val imageContent: ImageContent, override val deriverName: DerivateName) : ImageContent by imageContent, DerivedContent<BufferedImage>
