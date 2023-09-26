package org.vitrivr.engine.core.model.content.impl

import org.vitrivr.engine.core.model.content.decorators.DerivedContent
import org.vitrivr.engine.core.model.content.element.ImageContent
import org.vitrivr.engine.core.operators.derive.DerivateName

data class DerivedImageContent(private val imageContent: ImageContent, override val deriverName: DerivateName) : ImageContent by imageContent, DerivedContent
