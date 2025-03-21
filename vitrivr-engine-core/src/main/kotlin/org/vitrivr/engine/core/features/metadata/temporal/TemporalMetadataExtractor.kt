package org.vitrivr.engine.core.features.metadata.temporal

import org.vitrivr.engine.core.features.AbstractExtractor
import org.vitrivr.engine.core.model.content.element.ContentElement
import org.vitrivr.engine.core.model.descriptor.Descriptor
import org.vitrivr.engine.core.model.descriptor.struct.metadata.TemporalMetadataDescriptor
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.retrievable.Ingested
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.model.retrievable.attributes.time.TimePointAttribute
import org.vitrivr.engine.core.model.retrievable.attributes.time.TimeRangeAttribute
import org.vitrivr.engine.core.model.types.Value
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.core.operators.ingest.Extractor
import java.util.*

/**
 * An [Extractor] that extracts [TemporalMetadataDescriptor]s from [Ingested] objects.
 *
 * @author Ralph Gasser
 * @version 1.3.0
 */
class TemporalMetadataExtractor : AbstractExtractor<ContentElement<*>, TemporalMetadataDescriptor> {

    constructor(input: Operator<Retrievable>, analyser: TemporalMetadata, field: Schema.Field<ContentElement<*>, TemporalMetadataDescriptor>) : super(input, analyser, field)
    constructor(input: Operator<Retrievable>, analyser: TemporalMetadata, name: String) : super(input, analyser, name)

    override fun matches(retrievable: Retrievable): Boolean = retrievable.hasAttribute(TimePointAttribute::class.java) || retrievable.hasAttribute(TimeRangeAttribute::class.java)

    /**
     * Internal method to perform extraction on [Retrievable].
     **
     * @param retrievable The [Retrievable] to process.
     * @return List of resulting [Descriptor]s.
     */
    override fun extract(retrievable: Retrievable): List<TemporalMetadataDescriptor> {
        if (retrievable.hasAttribute(TimePointAttribute::class.java)) {
            val timestamp = retrievable.filteredAttribute(TimePointAttribute::class.java)!!
            return listOf(TemporalMetadataDescriptor(UUID.randomUUID(), retrievable.id, mapOf("start" to Value.Long(timestamp.timepointNs), "end" to Value.Long(timestamp.timepointNs)), this@TemporalMetadataExtractor.field))
        } else if (retrievable.hasAttribute(TimeRangeAttribute::class.java)) {
            val span = retrievable.filteredAttribute(TimeRangeAttribute::class.java)!!
            return listOf(TemporalMetadataDescriptor(UUID.randomUUID(), retrievable.id, mapOf("start" to Value.Long(span.startNs), "end" to Value.Long(span.endNs)), this@TemporalMetadataExtractor.field))
        }
        return emptyList()
    }
}