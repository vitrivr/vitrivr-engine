package org.vitrivr.engine.core.features.metadata.source.file

import org.vitrivr.engine.core.context.QueryContext
import org.vitrivr.engine.core.features.AbstractRetriever
import org.vitrivr.engine.core.model.content.element.ContentElement
import org.vitrivr.engine.core.model.descriptor.struct.metadata.source.FileSourceMetadataDescriptor
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.query.Query
import org.vitrivr.engine.core.operators.retrieve.Retriever

/**
 * A [Retriever] that performs lookup on [FileSourceMetadataDescriptor]s.
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
class FileSourceMetadataRetriever(field: Schema.Field<ContentElement<*>, FileSourceMetadataDescriptor>, query: Query, context: QueryContext) : AbstractRetriever<ContentElement<*>, FileSourceMetadataDescriptor>(field, query, context)
