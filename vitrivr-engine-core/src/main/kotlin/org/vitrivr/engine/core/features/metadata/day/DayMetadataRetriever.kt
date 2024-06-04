package org.vitrivr.engine.core.features.metadata.day

import org.vitrivr.engine.core.context.QueryContext
import org.vitrivr.engine.core.features.AbstractRetriever
import org.vitrivr.engine.core.model.content.element.ContentElement
import org.vitrivr.engine.core.model.descriptor.struct.metadata.DayMetadataDescriptor
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.query.Query
import org.vitrivr.engine.core.operators.retrieve.Retriever

/**
 * A [Retriever] that performs lookup on [DayMetadataDescriptor]s
 */
class DayMetadataRetriever(field: Schema.Field<ContentElement<*>, DayMetadataDescriptor>, query: Query, context: QueryContext): AbstractRetriever<ContentElement<*>, DayMetadataDescriptor>(field,query,context)
