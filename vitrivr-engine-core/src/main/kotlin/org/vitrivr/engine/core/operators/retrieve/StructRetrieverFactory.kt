package org.vitrivr.engine.core.operators.retrieve

import org.vitrivr.engine.core.model.content.element.ContentElement
import org.vitrivr.engine.core.model.descriptor.struct.StructDescriptor
import org.vitrivr.engine.core.model.metamodel.Schema

/**
 * The [StructRetrieverFactory] creates [Retriever]s for [StructDescriptor]s.
 */
interface StructRetrieverFactory {

    fun <C:ContentElement<*>>newRetriever(schema: Schema, properties: Map<String,String>): Retriever<C,StructDescriptor>
}
