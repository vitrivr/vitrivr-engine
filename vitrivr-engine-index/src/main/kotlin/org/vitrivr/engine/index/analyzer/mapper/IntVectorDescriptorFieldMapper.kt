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

class IntVectorDescriptorFieldMapper : DescriptorFieldMapper<IntVectorDescriptor>() {

    override fun cast(
        descriptor: Descriptor<*>,
        field: Schema.Field<ContentElement<*>, IntVectorDescriptor>
    ): IntVectorDescriptor = when (descriptor) {
        is FloatVectorDescriptor -> IntVectorDescriptor(
            descriptor.id,
            descriptor.retrievableId,
            Value.IntVector(IntArray(descriptor.vector.size) { descriptor.vector.value[it].toInt() }),
            field
        )

        is DoubleVectorDescriptor -> IntVectorDescriptor(
            descriptor.id,
            descriptor.retrievableId,
            Value.IntVector(IntArray(descriptor.vector.size) { descriptor.vector.value[it].toInt() }),
            field
        )

        is IntVectorDescriptor -> descriptor.copy(field = field)
        is LongVectorDescriptor -> IntVectorDescriptor(
            descriptor.id,
            descriptor.retrievableId,
            Value.IntVector(IntArray(descriptor.vector.size) { descriptor.vector.value[it].toInt() }),
            field
        )

        else -> throw IllegalArgumentException("${descriptor::class.simpleName} cannot be transformed to IntVectorDescriptor")
    }

    override val descriptorClass: KClass<IntVectorDescriptor> = IntVectorDescriptor::class

    override fun prototype(field: Schema.Field<*, *>): IntVectorDescriptor = IntVectorDescriptor(
        UUID.randomUUID(),
        UUID.randomUUID(),
        Value.IntVector(
            field.parameters[LENGTH_PARAMETER_NAME]?.toIntOrNull()
                ?: throw IllegalArgumentException("'$LENGTH_PARAMETER_NAME' is not defined")
        )
    )
}