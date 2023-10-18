package org.vitrivr.engine.base.features.external

import org.vitrivr.engine.core.model.content.element.ContentElement
import org.vitrivr.engine.core.model.database.descriptor.Descriptor
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
abstract class ExternalRetriever<C : ContentElement<*>, D : Descriptor> : Retriever<C, D> {

    /**
     * The query vector for the external feature retrieval.
     */
    abstract val queryVector: Descriptor
}
