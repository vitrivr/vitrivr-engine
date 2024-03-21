package org.vitrivr.engine.query.retriever

import org.vitrivr.engine.core.model.content.element.ContentElement
import org.vitrivr.engine.core.model.descriptor.struct.StructDescriptor
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.operators.retrieve.Retriever
import org.vitrivr.engine.core.operators.retrieve.StructRetrieverFactory


/**
 * The [StructBooleanRetrieverFactory] creates [StructBooleanRetriever] for a [Schema.Field].
 *
 * ```
 * {
 * "type":"RETRIEVER",
 *  "properties":{
 * "field":"file",
 * "property":"size",
 * "comparator":"<",
 * "query":"20000"}
 * }
 * ```
 */
class StructBooleanRetrieverFactory : StructRetrieverFactory {

    companion object{
        val FIELD_KEY = "field"
        val PROPERTY_KEY = "property"
        val COMPARATOR_KEY = "comparator"
        val QUERY_KEY = "query"
    }

    override fun <C : ContentElement<*>> newRetriever(
        schema: Schema,
        properties: Map<String, String>
    ): Retriever<C, StructDescriptor> {
        TODO("Not yet implemented")
    }
}
