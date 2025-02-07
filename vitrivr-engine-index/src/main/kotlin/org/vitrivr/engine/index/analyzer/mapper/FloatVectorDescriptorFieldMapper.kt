package org.vitrivr.engine.index.analyzer.mapper

import org.vitrivr.engine.core.model.content.element.ContentElement
import org.vitrivr.engine.core.model.descriptor.Descriptor
import org.vitrivr.engine.core.model.descriptor.vector.DoubleVectorDescriptor
import org.vitrivr.engine.core.model.descriptor.vector.FloatVectorDescriptor
import org.vitrivr.engine.core.model.descriptor.vector.IntVectorDescriptor
import org.vitrivr.engine.core.model.descriptor.vector.LongVectorDescriptor
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.types.Value
import java.util.*
import kotlin.reflect.KClass

class FloatVectorDescriptorFieldMapper : DescriptorFieldMapper<FloatVectorDescriptor>() {

    override fun cast(
        descriptor: Descriptor<*>,
        field: Schema.Field<ContentElement<*>, FloatVectorDescriptor>
    ): FloatVectorDescriptor = when (descriptor) {
        is FloatVectorDescriptor -> descriptor.copy(field = field)
        is DoubleVectorDescriptor -> FloatVectorDescriptor(
            descriptor.id,
            descriptor.retrievableId,
            Value.FloatVector(FloatArray(descriptor.vector.size) { descriptor.vector.value[it].toFloat() }),
            field
        )

        is IntVectorDescriptor -> FloatVectorDescriptor(
            descriptor.id,
            descriptor.retrievableId,
            Value.FloatVector(FloatArray(descriptor.vector.size) { descriptor.vector.value[it].toFloat() }),
            field
        )

        is LongVectorDescriptor -> FloatVectorDescriptor(
            descriptor.id,
            descriptor.retrievableId,
            Value.FloatVector(FloatArray(descriptor.vector.size) { descriptor.vector.value[it].toFloat() }),
            field
        )

        else -> throw IllegalArgumentException("${descriptor::class.simpleName} cannot be transformed to FloatVectorDescriptor")
    }


    override val descriptorClass: KClass<FloatVectorDescriptor> = FloatVectorDescriptor::class

    override fun prototype(field: Schema.Field<*, *>): FloatVectorDescriptor = FloatVectorDescriptor(
        UUID.randomUUID(), UUID.randomUUID(), Value.FloatVector(
            field.parameters[LENGTH_PARAMETER_NAME]?.toIntOrNull()
                ?: throw IllegalArgumentException("'$LENGTH_PARAMETER_NAME' is not defined")
        )
    )

}