package org.vitrivr.engine.base.features.external

import org.vitrivr.engine.core.context.QueryContext
import org.vitrivr.engine.core.database.descriptor.DescriptorReader
import org.vitrivr.engine.core.model.content.element.ContentElement
import org.vitrivr.engine.core.model.descriptor.Descriptor
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.operators.retrieve.Retriever

/**
 * Abstract class for implementing an external feature retriever.
 *
 * @param C Type of [ContentElement] that this external retriever operates on.
 * @param D Type of [Descriptor] produced by this external retriever.
 *
 * @see [ExternalAnalyser]
 *
 * @author Rahel Arnold
 * @version 1.0.0
 */
abstract class ExternalRetriever<C : ContentElement<*>, D : Descriptor>(override val field: Schema.Field<C, D>, val query: D, val context: QueryContext) : Retriever<C, D> {
    /** The [DescriptorReader] instance used by this [ExternalRetriever]. */
    protected val reader: DescriptorReader<D> by lazy { this.field.getReader() }
}