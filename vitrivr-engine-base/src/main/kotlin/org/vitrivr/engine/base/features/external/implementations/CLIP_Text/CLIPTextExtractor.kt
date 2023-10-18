package org.vitrivr.engine.base.features.external.implementations.CLIP_Text

import org.vitrivr.engine.base.features.external.ExternalExtractor
import org.vitrivr.engine.core.model.content.element.TextContent
import org.vitrivr.engine.core.model.database.descriptor.vector.FloatVectorDescriptor
import org.vitrivr.engine.core.model.database.retrievable.Ingested
import org.vitrivr.engine.core.model.database.retrievable.RetrievableWithContent
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.operators.Operator

/**
 * [CLIPTextExtractor] implementation of an [ExternalExtractor] for [CLIPTextFactory].
 *
 * @param field Schema field for which the extractor generates descriptors.
 * @param input Operator representing the input data source.
 * @param persisting Flag indicating whether the descriptors should be persisted.
 *
 * @author Rahel Arnold
 * @version 1.0.0
 */
class CLIPTextExtractor(
    override val field: Schema.Field<TextContent, FloatVectorDescriptor>,
    override val input: Operator<Ingested>,
    override val persisting: Boolean = true,
) : ExternalExtractor<TextContent, FloatVectorDescriptor>() {

    /**
     * Creates a descriptor for a given retrievable ID and content elements.
     *
     * @param retrievable The [RetrievableWithContent] for which to create the descriptor.
     * @return The created [FloatVectorDescriptor].
     */
    override fun createDescriptor(
        retrievable: RetrievableWithContent
    ): FloatVectorDescriptor {
        return FloatVectorDescriptor(
            retrievableId = retrievable.id, transient = !persisting, vector = queryExternalFeatureAPI(retrievable)
        )
    }

    /**
     * Queries the external feature API for the feature of the given content element.
     *
     * @param retrievable The [RetrievableWithContent] for which to query the external feature API.
     * @return The [List] of [Float] representing the obtained external feature.
     */
    override fun queryExternalFeatureAPI(retrievable: RetrievableWithContent): List<Float> {
        // Extract and parse the response from the external feature API
        return CLIPTextFactory.requestDescriptor(retrievable.content.filterIsInstance<TextContent>().first())
    }

}
