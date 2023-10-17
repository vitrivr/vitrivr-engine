package org.vitrivr.engine.base.features.external

import org.vitrivr.engine.core.model.content.Content
import org.vitrivr.engine.core.model.content.element.ContentElement
import org.vitrivr.engine.core.model.content.element.ImageContent
import org.vitrivr.engine.core.model.database.descriptor.Descriptor
import org.vitrivr.engine.core.model.database.descriptor.vector.FloatVectorDescriptor
import org.vitrivr.engine.core.model.metamodel.Analyser
import kotlin.reflect.KClass

/**
 * Implementation of the [ExternalAnalyser], which derives external features from an [ImageContent] as [FloatVectorDescriptor].
 *
 * @param host The host address of the external feature extraction service.
 * @param port The port of the external feature extraction service.
 * @param featureName The name of the external feature to extract.
 *
 *
 * @author Rahel Arnold
 * @version 1.0.0
 */
abstract class ExternalAnalyser<T: ContentElement<*>, U: Descriptor>(

) : Analyser<T, U> {
    abstract val host: String
    abstract val port: Int
    abstract val featureName: String
}
