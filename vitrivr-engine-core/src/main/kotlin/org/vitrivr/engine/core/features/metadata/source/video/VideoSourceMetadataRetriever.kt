package org.vitrivr.engine.core.features.metadata.source.video

import org.vitrivr.engine.core.context.QueryContext
import org.vitrivr.engine.core.features.AbstractRetriever
import org.vitrivr.engine.core.model.content.element.ContentElement
import org.vitrivr.engine.core.model.descriptor.struct.metadata.source.VideoSourceMetadataDescriptor
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.query.bool.BooleanQuery
import org.vitrivr.engine.core.operators.retrieve.Retriever

/**
 * A [Retriever] that performs lookup on [VideoSourceMetadataDescriptor]s.
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
class VideoSourceMetadataRetriever(field: Schema.Field<ContentElement<*>, VideoSourceMetadataDescriptor>, query: BooleanQuery, context: QueryContext) : AbstractRetriever<ContentElement<*>, VideoSourceMetadataDescriptor>(field, query, context)