package org.vitrivr.engine.core.features.metadata.temporal

import org.vitrivr.engine.core.features.AbstractExtractor
import org.vitrivr.engine.core.model.content.element.ContentElement
import org.vitrivr.engine.core.model.descriptor.Descriptor
import org.vitrivr.engine.core.model.descriptor.struct.metadata.TemporalMetadataDescriptor
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.retrievable.Ingested
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.model.retrievable.attributes.time.TimerangeAttribute
import org.vitrivr.engine.core.model.retrievable.attributes.time.TimestampAttribute
import org.vitrivr.engine.core.model.types.Value
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.core.operators.ingest.Extractor
import java.util.*

/**
 * An [Extractor] that extracts [TemporalMetadataDescriptor]s from [Ingested] objects.
 *
 * @author Ralph Gasser
 * @version 1.2.0
 */
class TemporalMetadataExtractor(input: Operator<Retrievable>, field: Schema.Field<ContentElement<*>, TemporalMetadataDescriptor>?) : AbstractExtractor<ContentElement<*>, TemporalMetadataDescriptor>(input, field) {

    override fun matches(retrievable: Retrievable): Boolean = retrievable.hasAttribute(TimestampAttribute::class.java) || retrievable.hasAttribute(TimerangeAttribute::class.java)

    /**
     * Internal method to perform extraction on [Retrievable].
     **
     * @param retrievable The [Retrievable] to process.
     * @return List of resulting [Descriptor]s.
     */
    override fun extract(retrievable: Retrievable): List<TemporalMetadataDescriptor> {
        if (retrievable.hasAttribute(TimestampAttribute::class.java)) {
            val timestamp = retrievable.filteredAttribute(TimestampAttribute::class.java)!!
            return listOf(TemporalMetadataDescriptor(UUID.randomUUID(), retrievable.id, Value.Long(timestamp.timestampNs), Value.Long(timestamp.timestampNs), this@TemporalMetadataExtractor.field))
        } else if (retrievable.hasAttribute(TimerangeAttribute::class.java)) {
            val span = retrievable.filteredAttribute(TimerangeAttribute::class.java)!!
            return listOf(TemporalMetadataDescriptor(UUID.randomUUID(), retrievable.id, Value.Long(span.startNs), Value.Long(span.endNs), this@TemporalMetadataExtractor.field))
        }
        return emptyList()
    }
}