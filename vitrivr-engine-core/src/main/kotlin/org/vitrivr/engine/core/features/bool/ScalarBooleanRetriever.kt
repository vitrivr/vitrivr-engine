package org.vitrivr.engine.core.features.bool

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.flow
import org.vitrivr.engine.core.context.QueryContext
import org.vitrivr.engine.core.features.AbstractRetriever
import org.vitrivr.engine.core.model.content.element.ContentElement
import org.vitrivr.engine.core.model.descriptor.scalar.ScalarDescriptor
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.query.bool.BooleanQuery

/**
 * A simple [AbstractRetriever] implementation for boolean queries on [ScalarDescriptor]s.
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
class ScalarBooleanRetriever<C : ContentElement<*>>(field: Schema.Field<C, ScalarDescriptor<*, *>>, query: BooleanQuery, context: QueryContext) : AbstractRetriever<C, ScalarDescriptor<*, *>>(field, query, context) {
    override fun toFlow(scope: CoroutineScope) = flow {
        val reader = this@ScalarBooleanRetriever.reader
        reader.queryAndJoin(this@ScalarBooleanRetriever.query).forEach {
            emit(it)
        }
    }
}
