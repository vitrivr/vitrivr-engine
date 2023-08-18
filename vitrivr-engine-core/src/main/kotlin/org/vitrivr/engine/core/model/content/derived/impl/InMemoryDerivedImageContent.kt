package org.vitrivr.engine.core.model.content.derived.impl

import org.vitrivr.engine.core.model.content.derived.DerivedImageContent
import org.vitrivr.engine.core.operators.derive.DerivateName
import org.vitrivr.engine.core.source.Source
import java.awt.image.BufferedImage

data class InMemoryDerivedImageContent(
    override val source: Source,
    override val timeNs: Long? = null,
    override val posX: Int? = null,
    override val posY: Int? = null,
    override val posZ: Int? = null,
    override val image: BufferedImage,
    override val name: DerivateName
) : DerivedImageContent