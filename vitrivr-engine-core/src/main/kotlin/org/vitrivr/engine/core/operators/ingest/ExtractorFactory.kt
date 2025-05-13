package org.vitrivr.engine.core.operators.ingest

import org.vitrivr.engine.core.context.IndexContext
import org.vitrivr.engine.core.model.content.element.ContentElement
import org.vitrivr.engine.core.model.descriptor.Descriptor
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.operators.Operator

/**
 * A factory object for a specific [Extractor] type.
 *
 * @author Ralph Gasser
 * @version 1.1.0
 */
interface ExtractorFactory<C : ContentElement<*>, D : Descriptor<*>> {
    /**
     * Creates a new [Extractor] instance from this [ExtractorFactory].
     *
     * @param field The [Schema.Field] to create the [Extractor] for.
     * @param input The input [Operator].
     * @param context The [IndexContext] to use.
     */
    fun newExtractor(field: Schema.Field<C, D>, input: Operator<Retrievable>, parameters: Map<String, String>, context: IndexContext): Extractor<C, D>

    /**
     * Creates a new [Extractor] instance from this [ExtractorFactory].
     *
     * @param name The name of the [Operator].
     * @param input The input [Operator].
     * @param context The [IndexContext] to use.
     */
    fun newExtractor(name: String, input: Operator<Retrievable>, parameters: Map<String, String>, context: IndexContext): Extractor<C, D>
}