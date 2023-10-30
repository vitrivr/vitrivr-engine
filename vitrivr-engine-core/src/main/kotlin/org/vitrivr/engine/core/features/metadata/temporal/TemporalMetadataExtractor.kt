package org.vitrivr.engine.core.features.metadata.temporal

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.vitrivr.engine.core.model.content.decorators.TemporalContent
import org.vitrivr.engine.core.model.content.element.ContentElement
import org.vitrivr.engine.core.model.descriptor.struct.metadata.TemporalMetadataDescriptor
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.retrievable.Ingested
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.model.retrievable.decorators.RetrievableWithContent
import org.vitrivr.engine.core.model.retrievable.decorators.RetrievableWithDescriptor
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.core.operators.ingest.Extractor
import java.util.*

/**
 * An [Extractor] that extracts [TemporalMetadataDescriptor]s from [Ingested] objects.
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
class TemporalMetadataExtractor(override val field: Schema.Field<ContentElement<*>, TemporalMetadataDescriptor>, override val input: Operator<Retrievable>, override val persisting: Boolean = true) : Extractor<ContentElement<*>, TemporalMetadataDescriptor> {

    /** */
    private val writer by lazy { this.field.getWriter() }

    /**
     *
     */
    override fun toFlow(scope: CoroutineScope): Flow<Retrievable> = this.input.toFlow(scope).map { retrievable ->
        if (retrievable is RetrievableWithContent) {
            for (c in retrievable.content) {
                if (c is TemporalContent) {
                    val descriptor = when (c) {
                        is TemporalContent.Timepoint -> TemporalMetadataDescriptor(UUID.randomUUID(), retrievable.id, c.timepointNs, c.timepointNs, !persisting)
                        is TemporalContent.TimeSpan -> TemporalMetadataDescriptor(UUID.randomUUID(), retrievable.id, c.startNs, c.endNs, !persisting)
                        else -> throw IllegalStateException("TemporalContent is neither timepoint nor time span.")
                    }

                    /* Append descriptor. */
                    if (retrievable is RetrievableWithDescriptor.Mutable) {
                        retrievable.addDescriptor(descriptor)
                    }

                    /* Persist descriptor. */
                    if (this.persisting) {
                        writer.add(descriptor)
                    }
                }
            }
        }
        retrievable
    }
}