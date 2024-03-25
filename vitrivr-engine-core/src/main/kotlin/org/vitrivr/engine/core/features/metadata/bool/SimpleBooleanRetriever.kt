package org.vitrivr.engine.core.features.metadata.bool

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.vitrivr.engine.core.context.QueryContext
import org.vitrivr.engine.core.model.content.element.ContentElement
import org.vitrivr.engine.core.model.descriptor.struct.SimpleBooleanQueryDescriptor
import org.vitrivr.engine.core.model.descriptor.struct.StructDescriptor
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.query.bool.SimpleBooleanQuery
import org.vitrivr.engine.core.model.retrievable.Retrieved
import org.vitrivr.engine.core.model.retrievable.attributes.PropertyAttribute
import org.vitrivr.engine.core.model.types.Type
import org.vitrivr.engine.core.operators.retrieve.Retriever

/**
 * The [SimpleBooleanRetriever] operates on [SimpleBooleanQueryDescriptor] as a query and performs simple, i.e. binary Boolean retrieval.
 * That is, the [SimpleBooleanRetriever] enables boolean queries on a [StructDescriptor]'s sub-field.
 */
class SimpleBooleanRetriever(
    override val field: Schema.Field<ContentElement<*>, StructDescriptor>,
    val query: SimpleBooleanQuery<*>,
    val context: QueryContext
) : Retriever<ContentElement<*>, StructDescriptor> {

    private val logger = KotlinLogging.logger {  }
    override fun toFlow(scope: CoroutineScope): Flow<Retrieved> {
        logger.debug { "Preparing flow for ${this.javaClass.simpleName}" }
        val reader = this.field.getReader()
        return flow {
            reader.getAll(query).forEach {
                emit(it)
            }
        }
    }
}
