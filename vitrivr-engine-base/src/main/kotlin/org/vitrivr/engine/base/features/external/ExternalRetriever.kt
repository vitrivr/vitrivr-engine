package org.vitrivr.engine.base.features.external

import org.vitrivr.engine.core.model.content.element.ContentElement
import org.vitrivr.engine.core.model.database.descriptor.Descriptor
import org.vitrivr.engine.core.operators.retrieve.Retriever

/**
 * [Retriever] implementation for external feature retrieval.
 *
 * @see [ExternalAnalyser]
 *
 * @author Rahel Arnold
 * @version 1.0.0
 */
abstract class ExternalRetriever<C : ContentElement<*>, D : Descriptor> : Retriever<C, D> {

    abstract val queryVector: Descriptor
}
