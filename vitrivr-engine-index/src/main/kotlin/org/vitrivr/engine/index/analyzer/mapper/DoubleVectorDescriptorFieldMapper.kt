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

class DoubleVectorDescriptorFieldMapper : DescriptorFieldMapper<DoubleVectorDescriptor>() {

    override fun cast(
        descriptor: Descriptor<*>,
        field: Schema.Field<ContentElement<*>, DoubleVectorDescriptor>
    ): DoubleVectorDescriptor = when (descriptor) {
        is FloatVectorDescriptor -> DoubleVectorDescriptor(
            descriptor.id,
            descriptor.retrievableId,
            Value.DoubleVector(DoubleArray(descriptor.vector.size) { descriptor.vector.value[it].toDouble() }),
            field
        )

        is DoubleVectorDescriptor -> descriptor.copy(field = field)
        is IntVectorDescriptor -> DoubleVectorDescriptor(
            descriptor.id,
            descriptor.retrievableId,
            Value.DoubleVector(DoubleArray(descriptor.vector.size) { descriptor.vector.value[it].toDouble() }),
            field
        )

        is LongVectorDescriptor -> DoubleVectorDescriptor(
            descriptor.id,
            descriptor.retrievableId,
            Value.DoubleVector(DoubleArray(descriptor.vector.size) { descriptor.vector.value[it].toDouble() }),
            field
        )

        else -> throw IllegalArgumentException("${descriptor::class.simpleName} cannot be transformed to DoubleVectorDescriptor")
    }

    override val descriptorClass: KClass<DoubleVectorDescriptor> = DoubleVectorDescriptor::class

    override fun prototype(field: Schema.Field<*, *>): DoubleVectorDescriptor = DoubleVectorDescriptor(
        UUID.randomUUID(),
        UUID.randomUUID(),
        Value.DoubleVector(
            field.parameters[LENGTH_PARAMETER_NAME]?.toIntOrNull()
                ?: throw IllegalArgumentException("'$LENGTH_PARAMETER_NAME' is not defined")
        )
    )
}