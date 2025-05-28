package org.vitrivr.engine.core.features.metadata.temporal

import org.vitrivr.engine.core.context.Context
import org.vitrivr.engine.core.features.AbstractRetriever
import org.vitrivr.engine.core.model.content.element.ContentElement
import org.vitrivr.engine.core.model.descriptor.struct.metadata.TemporalMetadataDescriptor
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.query.Query
import org.vitrivr.engine.core.operators.retrieve.Retriever

/**
 * A [Retriever] that performs lookup on [TemporalMetadataDescriptor]s.
 *
 * @author Ralph Gasser
 * @version 1.1.0
 */
class TemporalMetadataRetriever(field: Schema.Field<ContentElement<*>, TemporalMetadataDescriptor>, query: Query, context: Context) : AbstractRetriever<ContentElement<*>, TemporalMetadataDescriptor>(field, query, context)