package org.vitrivr.engine.core.features.metadata.source.exif

import org.vitrivr.engine.core.features.AbstractExtractor
import org.vitrivr.engine.core.model.content.element.ContentElement
import org.vitrivr.engine.core.model.descriptor.struct.metadata.source.MapStructDescriptor
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.model.retrievable.attributes.SourceAttribute
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.core.source.MediaType
import org.vitrivr.engine.core.source.file.FileSource

class ExifMetadataExtractor(
    input: Operator<Retrievable>,
    field: Schema.Field<ContentElement<*>, MapStructDescriptor>,
    persisting: Boolean = true
) : AbstractExtractor<ContentElement<*>, MapStructDescriptor>(input, field, persisting, bufferSize = 1) {
    override fun matches(retrievable: Retrievable): Boolean =
        retrievable.filteredAttribute(SourceAttribute::class.java)?.source is FileSource /* && retrievable.type is MediaType.IMAGE */

    override fun extract(retrievable: Retrievable): List<MapStructDescriptor> {
        TODO("Not yet implemented")
    }
}