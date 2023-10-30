package org.vitrivr.engine.base.features.external.implementations.dino

import org.vitrivr.engine.base.features.external.ExternalExtractor
import org.vitrivr.engine.core.context.IndexContext
import org.vitrivr.engine.core.model.content.element.ImageContent
import org.vitrivr.engine.core.model.descriptor.vector.FloatVectorDescriptor
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.model.retrievable.decorators.RetrievableWithContent
import org.vitrivr.engine.core.operators.Operator

/**
 * [DINOExtractor] implementation of an [ExternalExtractor] for [DINO].
 *
 * @param field Schema field for which the extractor generates descriptors.
 * @param input Operator representing the input data source.
 * @param persisting Flag indicating whether the descriptors should be persisted.
 *
 * @author Rahel Arnold
 * @version 1.0.0
 */
class DINOExtractor(input: Operator<Retrievable>, field: Schema.Field<ImageContent, FloatVectorDescriptor>, context: IndexContext, persisting: Boolean) : ExternalExtractor<ImageContent, FloatVectorDescriptor>(input, field, context, persisting) {

    /**
     * Creates a descriptor for a given retrievable ID and content elements.
     *
     * @param retrievable The [RetrievableWithContent] for which to create the descriptor.
     * @return The created [FloatVectorDescriptor].
     */
    override fun createDescriptor(retrievable: RetrievableWithContent) = FloatVectorDescriptor(retrievableId = retrievable.id, vector = queryExternalFeatureAPI(retrievable), transient = !this.persisting)


    /**
     * Queries the external feature API for the feature of the given content element.
     *
     * @param retrievable The [RetrievableWithContent] for which to query the external feature API.
     * @return The [List] of [Float] representing the obtained external feature.
     */
    override fun queryExternalFeatureAPI(retrievable: RetrievableWithContent): List<Float> = DINO.requestDescriptor(retrievable.content.filterIsInstance<ImageContent>().first())
}
