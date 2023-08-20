package org.vitrivr.base.engine.extract

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.vitrivr.engine.core.model.color.MutableRGBFloatColorContainer
import org.vitrivr.engine.core.model.color.RGBByteColorContainer
import org.vitrivr.engine.core.model.color.RGBFloatColorContainer
import org.vitrivr.engine.core.model.content.ImageContent
import org.vitrivr.engine.core.model.database.descriptor.Descriptor
import org.vitrivr.engine.core.model.database.descriptor.DescriptorId
import org.vitrivr.engine.core.model.database.descriptor.vector.FloatVectorDescriptor
import org.vitrivr.engine.core.model.database.descriptor.vector.VectorDescriptor
import org.vitrivr.engine.core.model.database.retrievable.IngestedRetrievable
import org.vitrivr.engine.core.model.database.retrievable.RetrievableId
import org.vitrivr.engine.core.operators.DescriberId
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.core.operators.derive.impl.AverageImageContentDeriver
import org.vitrivr.engine.core.operators.ingest.Extractor
import org.vitrivr.engine.core.util.extension.getRGBArray
import java.util.UUID
import kotlin.reflect.KClass
import kotlin.reflect.KTypeParameter

class AverageColor(
    override val instanceName: String,
    override val input: Operator<IngestedRetrievable>,
    override val persisting: Boolean = true,
) : Extractor<FloatVectorDescriptor> {

    /** */
    override val descriptorClass: KClass<FloatVectorDescriptor> = FloatVectorDescriptor::class

    /** */
    override val typeName: String = "AverageColor"


    /**
     *
     */
    override fun specimen(): FloatVectorDescriptor = FloatVectorDescriptor(
        UUID.randomUUID(),
        UUID.randomUUID(),
        true,
        this.id,
        listOf(0.0f, 0.0f, 0.0f)
    )

    override fun toFlow(): Flow<IngestedRetrievable> = input.toFlow().map { retrievable: IngestedRetrievable ->
        if (retrievable.content.any { c -> c is ImageContent }) {

            val averageImage = retrievable.getDerivedContent(AverageImageContentDeriver.derivateName) as? ImageContent

            if (averageImage != null) {
                val color = MutableRGBFloatColorContainer()
                var counter = 0
                averageImage.image.getRGBArray().forEach { c ->
                    color += RGBByteColorContainer.fromRGB(c)
                    ++counter
                }
                val averageColor =
                    RGBFloatColorContainer(color.red / counter, color.green / counter, color.blue / counter)

                val descriptor = object : VectorDescriptor<Float> {
                    override val vector: List<Float> = averageColor.toList()
                    override val id: DescriptorId = UUID.randomUUID()
                    override val retrievableId: RetrievableId = retrievable.id
                    override val describerId: DescriberId = this@AverageColor.id
                    override val transient: Boolean = false
                }
                retrievable.descriptors.add(descriptor)

                if (persisting) {
                    //TODO persist
                }

                //TODO attach information to retrievable
            }
        }
        retrievable
    }
}