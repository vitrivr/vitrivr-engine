package org.vitrivr.engine.core.model.query.string

import org.vitrivr.engine.core.model.descriptor.scalar.StringDescriptor
import org.vitrivr.engine.core.model.query.Query

data class TextQuery(
        override val descriptor: StringDescriptor
) : Query<StringDescriptor>
