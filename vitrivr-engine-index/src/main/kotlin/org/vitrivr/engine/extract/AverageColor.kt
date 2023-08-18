package org.vitrivr.engine.extract

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.vitrivr.engine.core.model.color.MutableRGBFloatColorContainer
import org.vitrivr.engine.core.model.color.RGBByteColorContainer
import org.vitrivr.engine.core.model.color.RGBFloatColorContainer
import org.vitrivr.engine.core.model.content.ImageContent
import org.vitrivr.engine.core.model.database.descriptor.DescriptorId
import org.vitrivr.engine.core.model.database.descriptor.vector.VectorDescriptor
import org.vitrivr.engine.core.model.database.retrievable.IngestedRetrievable
import org.vitrivr.engine.core.model.database.retrievable.RetrievableId
import org.vitrivr.engine.core.operators.DescriberId
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.core.operators.derive.impl.AverageImageContentDeriver
import org.vitrivr.engine.core.operators.ingest.Extractor
import java.util.UUID

class AverageColor(
    override val instanceName: String,
    override val input: Operator<IngestedRetrievable>,
    override val persisting: Boolean = true
) : Extractor {

    override fun toFlow(): Flow<IngestedRetrievable> = input.toFlow().map { retrievable: IngestedRetrievable ->
        if (retrievable.content.any { c -> c is ImageContent }) {


            val averageImage = retrievable.getDerivedContent(AverageImageContentDeriver.derivateName) as? ImageContent

            if (averageImage != null) {
                val color = MutableRGBFloatColorContainer(0f, 0f, 0f)
                var counter = 0
                averageImage.image.getRGB(
                    0,
                    0,
                    averageImage.image.width,
                    averageImage.image.height,
                    null,
                    0,
                    averageImage.image.width
                ).forEach { c ->
                    color += RGBByteColorContainer.fromRGB(c)
                    ++counter
                }
                val averageColor =
                    RGBFloatColorContainer(color.red / counter, color.green / counter, color.blue / counter)
                val descriptor = object : VectorDescriptor<Float> {
                    override val vector: List<Float> = listOf(averageColor.red, averageColor.green, averageColor.blue)
                    override val id: DescriptorId = UUID.randomUUID()
                    override val retrievableId: RetrievableId = retrievable.id
                    override val describerId: DescriberId = this@AverageColor.id
                    override val transient: Boolean = true
                }
                retrievable.descriptors.add(descriptor)

                if (persisting) {
                    //TODO persist
                }

                //TODO attach information to retrievable
            }

            retrievable

        } else {
            retrievable
        }
    }
}