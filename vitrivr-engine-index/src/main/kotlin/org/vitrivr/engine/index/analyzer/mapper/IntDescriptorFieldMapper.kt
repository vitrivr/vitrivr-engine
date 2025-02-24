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

class IntDescriptorFieldMapper : DescriptorFieldMapper<IntDescriptor>() {

    override fun cast(descriptor: Descriptor<*>, field: Schema.Field<ContentElement<*>, IntDescriptor>): IntDescriptor =
        when (descriptor) {
            is FloatDescriptor -> IntDescriptor(
                descriptor.id,
                descriptor.retrievableId,
                Value.Int(descriptor.value.value.toInt()),
                field
            )

            is DoubleDescriptor -> IntDescriptor(
                descriptor.id,
                descriptor.retrievableId,
                Value.Int(descriptor.value.value.toInt()),
                field
            )

            is IntDescriptor -> descriptor.copy(field = field)
            is LongDescriptor -> IntDescriptor(
                descriptor.id,
                descriptor.retrievableId,
                Value.Int(descriptor.value.value.toInt()),
                field
            )

            else -> throw IllegalArgumentException("${descriptor::class.simpleName} cannot be transformed to IntDescriptor")
        }

    override val descriptorClass: KClass<IntDescriptor> = IntDescriptor::class

    override fun prototype(field: Schema.Field<*, *>): IntDescriptor =
        IntDescriptor(UUID.randomUUID(), UUID.randomUUID(), Value.Int(0))
}