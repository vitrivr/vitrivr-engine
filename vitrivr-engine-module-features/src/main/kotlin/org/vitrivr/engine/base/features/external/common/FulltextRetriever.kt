package org.vitrivr.engine.base.features.external.common

import org.vitrivr.engine.core.context.QueryContext
import org.vitrivr.engine.core.features.AbstractRetriever
import org.vitrivr.engine.core.model.content.element.ContentElement
import org.vitrivr.engine.core.model.descriptor.scalar.StringDescriptor
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.query.fulltext.SimpleFulltextQuery
import org.vitrivr.engine.core.operators.retrieve.Retriever

/**
 * An implementation of a [Retriever], that executes fulltext queries.
 *
 * @author Ralph Gasser
 * @version 1.1.0
 */
class FulltextRetriever(field: Schema.Field<ContentElement<*>, StringDescriptor>, query: SimpleFulltextQuery, context: QueryContext) : AbstractRetriever<ContentElement<*>, StringDescriptor>(field, query, context)