package org.vitrivr.engine.core.operators.derive

import org.vitrivr.engine.core.content.ContentFactory
import org.vitrivr.engine.core.model.content.decorators.DerivedContent
import org.vitrivr.engine.core.model.retrievable.Ingested

typealias DerivateName = String

interface ContentDeriver<out T : DerivedContent?> {

    val derivateName: DerivateName

    fun derive(retrievable: Ingested, contentFactory: ContentFactory): T

}