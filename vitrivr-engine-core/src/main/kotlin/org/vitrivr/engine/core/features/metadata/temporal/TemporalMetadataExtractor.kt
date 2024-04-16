package org.vitrivr.engine.core.features.metadata.temporal

import org.vitrivr.engine.core.features.AbstractExtractor
import org.vitrivr.engine.core.model.content.decorators.TemporalContent
import org.vitrivr.engine.core.model.content.element.ContentElement
import org.vitrivr.engine.core.model.descriptor.Descriptor
import org.vitrivr.engine.core.model.descriptor.struct.metadata.TemporalMetadataDescriptor
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.retrievable.Ingested
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.model.retrievable.attributes.ContentAttribute
import org.vitrivr.engine.core.model.types.Value
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.core.operators.ingest.Extractor
import java.util.*

/**
 * An [Extractor] that extracts [TemporalMetadataDescriptor]s from [Ingested] objects.
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
class TemporalMetadataExtractor(input: Operator<Retrievable>, field: Schema.Field<ContentElement<*>, TemporalMetadataDescriptor>?) : AbstractExtractor<ContentElement<*>, TemporalMetadataDescriptor>(input, field) {

    override fun matches(retrievable: Retrievable): Boolean = retrievable.filteredAttribute(ContentAttribute::class.java) != null

    /**
     * Internal method to perform extraction on [Retrievable].
     **
     * @param retrievable The [Retrievable] to process.
     * @return List of resulting [Descriptor]s.
     */
    override fun extract(retrievable: Retrievable): List<TemporalMetadataDescriptor> {
        val content = retrievable.filteredAttributes(ContentAttribute::class.java).map { it.content }
        val descriptors = content.filterIsInstance<TemporalContent>().map { c ->
            when (c) {
                is TemporalContent.Timepoint -> TemporalMetadataDescriptor(UUID.randomUUID(), retrievable.id, Value.Long(c.timepointNs), Value.Long(c.timepointNs), this@TemporalMetadataExtractor.field)
                is TemporalContent.TimeSpan -> TemporalMetadataDescriptor(UUID.randomUUID(), retrievable.id, Value.Long(c.startNs), Value.Long(c.endNs), this@TemporalMetadataExtractor.field)
            }
        }
        return descriptors
    }
}