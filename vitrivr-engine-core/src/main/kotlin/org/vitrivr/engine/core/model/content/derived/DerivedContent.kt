package org.vitrivr.engine.core.model.content.derived

import org.vitrivr.engine.core.model.content.Content
import org.vitrivr.engine.core.operators.derive.DerivateName

interface DerivedContent : Content {

    val name: DerivateName

}