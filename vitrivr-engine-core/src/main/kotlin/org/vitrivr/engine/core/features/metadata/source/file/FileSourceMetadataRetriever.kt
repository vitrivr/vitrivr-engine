package org.vitrivr.engine.core.features.metadata.source.file

import org.vitrivr.engine.core.context.Context
import org.vitrivr.engine.core.features.AbstractRetriever
import org.vitrivr.engine.core.model.content.element.ContentElement
import org.vitrivr.engine.core.model.descriptor.struct.metadata.source.FileSourceMetadataDescriptor
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.query.bool.BooleanQuery
import org.vitrivr.engine.core.operators.retrieve.Retriever

/**
 * A [Retriever] that performs lookup on [FileSourceMetadataDescriptor]s.
 *
 * @author Ralph Gasser
 * @version 1.1.0
 */
class FileSourceMetadataRetriever(field: Schema.Field<ContentElement<*>, FileSourceMetadataDescriptor>, query: BooleanQuery, context: Context) : AbstractRetriever<ContentElement<*>, FileSourceMetadataDescriptor>(field, query, context)
