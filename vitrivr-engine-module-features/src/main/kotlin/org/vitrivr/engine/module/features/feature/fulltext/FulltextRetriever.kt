package org.vitrivr.engine.module.features.feature.fulltext

import org.vitrivr.engine.core.context.QueryContext
import org.vitrivr.engine.core.features.AbstractRetriever
import org.vitrivr.engine.core.model.content.element.ContentElement
import org.vitrivr.engine.core.model.descriptor.scalar.StringDescriptor
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.query.fulltext.SimpleFulltextQuery

/**
 * An implementation of a [Retriever], that executes fulltext queries.
 *
 * @author Ralph Gasser, Fynn Faber
 * @version 1.2.0
 */
class FulltextRetriever<C: ContentElement<*>>(field: Schema.Field<C, StringDescriptor>, query: SimpleFulltextQuery, context: QueryContext) : AbstractRetriever<C, StringDescriptor>(field, query, context)