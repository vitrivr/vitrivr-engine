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

class FloatDescriptorFieldMapper : DescriptorFieldMapper<FloatDescriptor>() {

    override val descriptorClass: KClass<FloatDescriptor> = FloatDescriptor::class

    override fun prototype(field: Schema.Field<*, *>): FloatDescriptor =
        FloatDescriptor(UUID.randomUUID(), UUID.randomUUID(), Value.Float(0f))

    override fun cast(
        descriptor: Descriptor<*>,
        field: Schema.Field<ContentElement<*>, FloatDescriptor>
    ): FloatDescriptor = when (descriptor) {
        is FloatDescriptor -> descriptor.copy(field = field)
        is DoubleDescriptor -> FloatDescriptor(
            descriptor.id,
            descriptor.retrievableId,
            Value.Float(descriptor.value.value.toFloat()),
            field
        )

        is IntDescriptor -> FloatDescriptor(
            descriptor.id,
            descriptor.retrievableId,
            Value.Float(descriptor.value.value.toFloat()),
            field
        )

        is LongDescriptor -> FloatDescriptor(
            descriptor.id,
            descriptor.retrievableId,
            Value.Float(descriptor.value.value.toFloat()),
            field
        )

        else -> throw IllegalArgumentException("${descriptor::class.simpleName} cannot be transformed to FloatDescriptor")
    }
}