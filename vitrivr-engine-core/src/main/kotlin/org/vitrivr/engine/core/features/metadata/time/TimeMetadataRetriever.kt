package org.vitrivr.engine.core.features.metadata.time

import org.vitrivr.engine.core.context.QueryContext
import org.vitrivr.engine.core.features.AbstractRetriever
import org.vitrivr.engine.core.model.content.element.ContentElement
import org.vitrivr.engine.core.model.descriptor.struct.metadata.TimeMetadataDescriptor
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.query.Query
import org.vitrivr.engine.core.operators.retrieve.Retriever

/**
 * A [Retriever] that performs lookup on [TimeMetadata]s
 */
class TimeMetadataRetriever(field: Schema.Field<ContentElement<*>, TimeMetadataDescriptor>, query: Query, context: QueryContext): AbstractRetriever<ContentElement<*>, TimeMetadataDescriptor>(field, query, context)
