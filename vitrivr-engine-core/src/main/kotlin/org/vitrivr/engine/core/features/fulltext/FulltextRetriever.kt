package org.vitrivr.engine.core.features.fulltext

import org.vitrivr.engine.core.context.Context
import org.vitrivr.engine.core.features.AbstractRetriever
import org.vitrivr.engine.core.model.content.element.ContentElement
import org.vitrivr.engine.core.model.descriptor.scalar.TextDescriptor
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.query.fulltext.SimpleFulltextQuery

/**
 * An implementation of a [AbstractRetriever], that executes fulltext queries.
 *
 * @author Ralph Gasser
 * @author Fynn Faber
 * @version 1.3.0
 */
class FulltextRetriever<C: ContentElement<*>>(field: Schema.Field<C, TextDescriptor>, query: SimpleFulltextQuery, context: Context) : AbstractRetriever<C, TextDescriptor>(field, query, context)