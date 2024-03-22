package org.vitrivr.engine.base.features.fulltext

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.vitrivr.engine.core.context.QueryContext
import org.vitrivr.engine.core.model.content.element.ContentElement
import org.vitrivr.engine.core.model.descriptor.scalar.StringDescriptor
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.query.fulltext.SimpleFulltextQuery
import org.vitrivr.engine.core.model.retrievable.Retrieved
import org.vitrivr.engine.core.operators.retrieve.Retriever

/**
 * An implementation of a [Retriever], that executes fulltext queries.
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
class FulltextRetriever(override val field: Schema.Field<ContentElement<*>, StringDescriptor>, private val query: StringDescriptor, private val context: QueryContext) : Retriever<ContentElement<*>, StringDescriptor> {
    override fun toFlow(scope: CoroutineScope): Flow<Retrieved> {
        val limit = this.context.getProperty(this.field.fieldName, "limit")?.toLongOrNull() ?: Long.MAX_VALUE
        val reader = this.field.getReader()
        val query = SimpleFulltextQuery(value = this.query.value, limit = limit)
        return flow {
            reader.getAll(query).forEach {
                emit(it)
            }
        }
    }
}