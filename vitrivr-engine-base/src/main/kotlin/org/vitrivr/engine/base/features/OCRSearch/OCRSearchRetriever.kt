package org.vitrivr.engine.base.features.OCRSearch

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.vitrivr.engine.core.context.QueryContext
import org.vitrivr.engine.core.model.content.element.ImageContent
import org.vitrivr.engine.core.model.descriptor.scalar.StringDescriptor
import org.vitrivr.engine.core.model.retrievable.Retrieved
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.query.string.TextQuery
import org.vitrivr.engine.core.operators.retrieve.Retriever

class OCRSearchRetriever(override val field: Schema.Field<ImageContent, StringDescriptor>, private val queryStringDescriptor : StringDescriptor, private val queryContext: QueryContext) : Retriever<ImageContent, StringDescriptor> {
    override fun toFlow(scope: CoroutineScope): Flow<Retrieved> {
        val reader = this.field.getReader()
        val query = TextQuery(
                descriptor = this.queryStringDescriptor,
        )

        return flow {
            reader.getAll(query).forEach {
                emit(it)
            }
        }
    }
}