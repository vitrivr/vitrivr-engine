package org.vitrivr.engine.base.features.averagecolor

import org.vitrivr.engine.core.model.database.descriptor.vector.FloatVectorDescriptor
import org.vitrivr.engine.core.model.database.retrievable.IngestedRetrievable
import org.vitrivr.engine.core.model.metamodel.Analyser
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.core.operators.retrieve.Retriever
import java.util.*
import kotlin.reflect.KClass

/**
 *
 * @author Ralph Gasser
 * @version 1.0
 */
class AverageColor: Analyser<FloatVectorDescriptor> {
    override val analyserName: String = "AverageColor"
    override val descriptorClass: KClass<FloatVectorDescriptor> = FloatVectorDescriptor::class
    override fun newDescriptor() = FloatVectorDescriptor(UUID.randomUUID(), UUID.randomUUID(), true, listOf(0.0f, 0.0f, 0.0f))
    override fun newExtractor(input: Operator<IngestedRetrievable>, persisting: Boolean) = AverageColorExtractor(input, persisting)
    override fun newRetriever(): Retriever<FloatVectorDescriptor> {
        TODO("Not yet implemented")
    }
}