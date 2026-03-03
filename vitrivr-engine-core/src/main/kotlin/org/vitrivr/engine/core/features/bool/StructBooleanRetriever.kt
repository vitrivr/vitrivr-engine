package org.vitrivr.engine.core.features.bool

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.flow
import org.vitrivr.engine.core.context.Context
import org.vitrivr.engine.core.features.AbstractRetriever
import org.vitrivr.engine.core.model.content.element.ContentElement
import org.vitrivr.engine.core.model.descriptor.struct.StructDescriptor
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.query.bool.BooleanQuery

/**
 * A simple [AbstractRetriever] implementation for boolean queries on [StructDescriptor]s.
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
class StructBooleanRetriever<C : ContentElement<*>, D : StructDescriptor<*>>(field: Schema.Field<C, D>, query: BooleanQuery, context: Context) : AbstractRetriever<C, D>(field, query, context) {
    override fun toFlow(scope: CoroutineScope) = flow {
        val reader = this@StructBooleanRetriever.reader
        reader.queryAndJoin(this@StructBooleanRetriever.query).forEach {
            emit(it)
        }
    }
}