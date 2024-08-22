package org.vitrivr.engine.core.features.fulltext

import org.vitrivr.engine.core.context.QueryContext
import org.vitrivr.engine.core.features.AbstractRetriever
import org.vitrivr.engine.core.model.content.element.ContentElement
import org.vitrivr.engine.core.model.descriptor.scalar.TextDescriptor
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.query.fulltext.SimpleFulltextQuery

/**
 * An implementation of a [Retriever], that executes fulltext queries.
 *
 * @author Ralph Gasser, Fynn Faber
 * @version 1.2.0
 */
class FulltextRetriever<C: ContentElement<*>>(field: Schema.Field<C, TextDescriptor>, query: SimpleFulltextQuery, context: QueryContext) : AbstractRetriever<C, TextDescriptor>(field, query, context)