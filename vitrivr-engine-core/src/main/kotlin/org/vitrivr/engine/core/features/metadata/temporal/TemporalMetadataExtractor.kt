package org.vitrivr.engine.core.features.metadata.temporal

import org.vitrivr.engine.core.features.AbstractExtractor
import org.vitrivr.engine.core.features.metadata.source.file.FileSourceMetadataExtractor
import org.vitrivr.engine.core.model.content.decorators.TemporalContent
import org.vitrivr.engine.core.model.content.element.ContentElement
import org.vitrivr.engine.core.model.descriptor.Descriptor
import org.vitrivr.engine.core.model.descriptor.struct.metadata.TemporalMetadataDescriptor
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.retrievable.Ingested
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.model.retrievable.decorators.RetrievableWithContent
import org.vitrivr.engine.core.model.retrievable.decorators.RetrievableWithSource
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.core.operators.ingest.Extractor
import org.vitrivr.engine.core.source.file.FileSource
import java.util.*

/**
 * An [Extractor] that extracts [TemporalMetadataDescriptor]s from [Ingested] objects.
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
class TemporalMetadataExtractor(input: Operator<Retrievable>, field: Schema.Field<ContentElement<*>, TemporalMetadataDescriptor>, persisting: Boolean = true) :
    AbstractExtractor<ContentElement<*>, TemporalMetadataDescriptor>(input, field, persisting, bufferSize = 1) {
    /**
     * Internal method to check, if [Retrievable] matches this [Extractor] and should thus be processed.
     *
     * [FileSourceMetadataExtractor] implementation only works with [RetrievableWithSource] that contain a [FileSource].
     *
     * @param retrievable The [Retrievable] to check.
     * @return True on match, false otherwise,
     */
    override fun matches(retrievable: Retrievable): Boolean = retrievable is RetrievableWithContent

    /**
     * Internal method to perform extraction on [Retrievable].
     **
     * @param retrievable The [Retrievable] to process.
     * @return List of resulting [Descriptor]s.
     */
    override fun extract(retrievable: Retrievable): List<TemporalMetadataDescriptor> {
        check(retrievable is RetrievableWithContent) { "Incoming retrievable is not a retrievable with source. This is a programmer's error!" }
        val descriptors = retrievable.content.filterIsInstance<TemporalContent>().map { c ->
            when (c) {
                is TemporalContent.Timepoint -> TemporalMetadataDescriptor(UUID.randomUUID(), retrievable.id, c.timepointNs, c.timepointNs, !persisting)
                is TemporalContent.TimeSpan -> TemporalMetadataDescriptor(UUID.randomUUID(), retrievable.id, c.startNs, c.endNs, !persisting)
            }
        }
        return descriptors
    }
}