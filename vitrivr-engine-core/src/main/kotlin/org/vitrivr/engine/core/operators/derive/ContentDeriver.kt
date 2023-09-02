package org.vitrivr.engine.core.operators.derive

import org.vitrivr.engine.core.model.content.DerivedContent
import org.vitrivr.engine.core.model.database.retrievable.IngestedRetrievable

typealias DerivateName = String

interface ContentDeriver<out T : DerivedContent?> {

    val derivateName: DerivateName

    fun derive(retrievable: IngestedRetrievable): T

}