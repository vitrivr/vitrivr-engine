package org.vitrivr.engine.base.features.external.implementations.CLIP_Image

import org.vitrivr.engine.base.features.external.ExternalExtractor
import org.vitrivr.engine.core.model.content.element.ImageContent
import org.vitrivr.engine.core.model.database.descriptor.vector.FloatVectorDescriptor
import org.vitrivr.engine.core.model.database.retrievable.Ingested
import org.vitrivr.engine.core.model.database.retrievable.RetrievableWithContent
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.operators.Operator
import java.io.*
import java.util.*

/**
 * [CLIPImageExtractor] implementation of an [ExternalExtractor] for [CLIPImageFactory].
 *
 * @param field Schema field for which the extractor generates descriptors.
 * @param input Operator representing the input data source.
 * @param persisting Flag indicating whether the descriptors should be persisted.
 * @param host The host address of the external feature API.
 * @param port The port number of the external feature API.
 * @param featureName The name of the feature provided by the external API.
 *
 * @author Rahel Arnold
 * @version 1.0.0
 */
class CLIPImageExtractor(
    override val field: Schema.Field<ImageContent, FloatVectorDescriptor>,
    override val input: Operator<Ingested>,
    override val persisting: Boolean = true,
) : ExternalExtractor<ImageContent, FloatVectorDescriptor>() {

    /**
     * Creates a descriptor for a given retrievable ID and content elements.
     *
     * @param retrievableId The retrievable ID.
     * @param content The list of content elements.
     * @return The created FloatVectorDescriptor.
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
     * @param content The content element to send to the external feature API.
     * @return The List<Float> representing the obtained external feature.
     */
    override fun queryExternalFeatureAPI(retrievable: RetrievableWithContent): List<Float> {
        // Extract and parse the response from the external feature API
        return CLIPImageFactory.requestDescriptor(retrievable.content.filterIsInstance<ImageContent>().first())
    }

}
