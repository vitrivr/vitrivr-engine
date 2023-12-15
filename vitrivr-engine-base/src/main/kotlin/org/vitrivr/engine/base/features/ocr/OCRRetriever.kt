package org.vitrivr.engine.base.features.ocr

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.vitrivr.engine.core.context.QueryContext
import org.vitrivr.engine.core.model.content.element.ContentElement
import org.vitrivr.engine.core.model.descriptor.scalar.StringDescriptor
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.query.string.TextQuery
import org.vitrivr.engine.core.model.retrievable.Retrieved
import org.vitrivr.engine.core.operators.retrieve.Retriever

/**
 * [Retriever] implementation for the [OCR] analyser.
 *
 * @author Fynn Faber
 * @version 1.0.0
 */
class OCRRetriever(override val field: Schema.Field<ContentElement<*>, StringDescriptor>, private val query: StringDescriptor, private val context: QueryContext) : Retriever<ContentElement<*>, StringDescriptor> {
    override fun toFlow(scope: CoroutineScope): Flow<Retrieved> {
        val reader = this.field.getReader()
        val query = TextQuery(descriptor = this.query)
        return flow {
            reader.getAll(query).forEach {
                emit(it)
            }
        }
    }
}