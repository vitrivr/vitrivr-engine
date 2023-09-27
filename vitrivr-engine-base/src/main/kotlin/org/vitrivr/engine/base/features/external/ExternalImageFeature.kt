package org.vitrivr.engine.base.features.external

import org.vitrivr.engine.core.model.content.element.ImageContent
import org.vitrivr.engine.core.model.database.descriptor.vector.FloatVectorDescriptor
import org.vitrivr.engine.core.model.metamodel.Analyser
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.util.DescriptorList
import java.util.*


abstract class ExternalImageFeature : Analyser<ImageContent, FloatVectorDescriptor> {

    val DEFAULT_API_ENDPOINT = "http://localhost:8888"

    override fun prototype() =
        FloatVectorDescriptor(UUID.randomUUID(), UUID.randomUUID(), listOf(0.0f, 0.0f, 0.0f), true)

    /**
    NewRetriever are the same for all external image features, as there is a similarity search performed using [ExternalImageRetriever]
     */
    override fun newRetriever(field: Schema.Field<ImageContent,FloatVectorDescriptor>, content: Collection<ImageContent>): ExternalImageRetriever {
        require(field.analyser == this) { }
        return newRetriever(field, this.analyse(content))
    }


    override fun newRetriever(field: Schema.Field<ImageContent,FloatVectorDescriptor>, descriptors: DescriptorList<FloatVectorDescriptor>): ExternalImageRetriever {
        require(field.analyser == this) { }
        return ExternalImageRetriever(field, descriptors.first())
    }

}