package org.vitrivr.engine.core.model.retrievable.attributes

import org.vitrivr.engine.core.source.Source

data class SourceAttribute(val source: Source) : MergingRetrievableAttribute {
    override fun merge(other: MergingRetrievableAttribute): MergingRetrievableAttribute {
        throw IllegalStateException("Cannot have multiple source attributes per retrievable")
    }

}
