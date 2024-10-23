package org.vitrivr.engine.index.analyzer.mapper

import org.vitrivr.engine.core.model.content.element.ContentElement
import org.vitrivr.engine.core.model.descriptor.Descriptor
import org.vitrivr.engine.core.model.descriptor.scalar.DoubleDescriptor
import org.vitrivr.engine.core.model.descriptor.scalar.FloatDescriptor
import org.vitrivr.engine.core.model.descriptor.scalar.IntDescriptor
import org.vitrivr.engine.core.model.descriptor.scalar.LongDescriptor
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.types.Value
import java.util.*
import kotlin.reflect.KClass

class DoubleDescriptorFieldMapper : DescriptorFieldMapper<DoubleDescriptor>() {

    override fun cast(
        descriptor: Descriptor<*>,
        field: Schema.Field<ContentElement<*>, DoubleDescriptor>
    ): DoubleDescriptor = when(descriptor) {
        is FloatDescriptor -> DoubleDescriptor(
            descriptor.id,
            descriptor.retrievableId,
            Value.Double(descriptor.value.value.toDouble()),
            field
        )
        is DoubleDescriptor -> descriptor.copy(field = field)
        is IntDescriptor -> DoubleDescriptor(
            descriptor.id,
            descriptor.retrievableId,
            Value.Double(descriptor.value.value.toDouble()),
            field
        )
        is LongDescriptor -> DoubleDescriptor(
            descriptor.id,
            descriptor.retrievableId,
            Value.Double(descriptor.value.value.toDouble()),
            field
        )
        else -> throw IllegalArgumentException("${descriptor::class.simpleName} cannot be transformed to DoubleDescriptor")
    }

    override fun prototype(field: Schema.Field<*, *>): DoubleDescriptor = DoubleDescriptor(UUID.randomUUID(), UUID.randomUUID(), Value.Double(0.0))

    override val descriptorClass: KClass<DoubleDescriptor> = DoubleDescriptor::class
}