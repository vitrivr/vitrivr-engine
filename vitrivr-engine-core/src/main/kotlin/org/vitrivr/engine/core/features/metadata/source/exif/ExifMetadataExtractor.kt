package org.vitrivr.engine.core.features.metadata.source.exif

import org.vitrivr.engine.core.features.AbstractExtractor
import org.vitrivr.engine.core.model.content.element.ContentElement
import org.vitrivr.engine.core.model.descriptor.struct.metadata.source.MapStructDescriptor
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.operators.Operator

class ExifMetadataExtractor(
    input: Operator<Retrievable>,
    field: Schema.Field<ContentElement<*>, MapStructDescriptor>,
    persisting: Boolean = true
) : AbstractExtractor<ContentElement<*>, MapStructDescriptor>(input, field, persisting, bufferSize = 1) {
    override fun matches(retrievable: Retrievable): Boolean {
        TODO("Not yet implemented")
    }

    override fun extract(retrievable: Retrievable): List<MapStructDescriptor> {
        TODO("Not yet implemented")
    }
}