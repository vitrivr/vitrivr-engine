package org.vitrivr.engine.core.features.metadata.source.exif

import org.vitrivr.engine.core.context.Context
import org.vitrivr.engine.core.features.AbstractRetriever
import org.vitrivr.engine.core.model.content.element.ContentElement
import org.vitrivr.engine.core.model.descriptor.struct.AnyMapStructDescriptor
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.query.Query

class ExifMetadataRetriever(field: Schema.Field<ContentElement<*>, AnyMapStructDescriptor>, query: Query, context: Context) : AbstractRetriever<ContentElement<*>, AnyMapStructDescriptor>(field, query, context)