package org.vitrivr.engine.database.jsonl

import kotlinx.serialization.Serializable
import org.vitrivr.engine.core.model.descriptor.Attribute
import org.vitrivr.engine.core.model.types.Value

@Serializable
data class AttributeContainer(val attribute: Attribute, val value: ValueContainer?) {
    constructor(attribute: Attribute, value: Value<*>?) : this(
        attribute,
        if (value == null) null else ValueContainer.fromValue(value)
    )
}
