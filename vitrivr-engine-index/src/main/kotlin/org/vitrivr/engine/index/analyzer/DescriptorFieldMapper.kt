package org.vitrivr.engine.index.analyzer

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.onEach
import org.vitrivr.engine.core.context.IndexContext
import org.vitrivr.engine.core.context.QueryContext
import org.vitrivr.engine.core.model.content.element.ContentElement
import org.vitrivr.engine.core.model.descriptor.Descriptor
import org.vitrivr.engine.core.model.descriptor.scalar.DoubleDescriptor
import org.vitrivr.engine.core.model.descriptor.scalar.FloatDescriptor
import org.vitrivr.engine.core.model.descriptor.scalar.IntDescriptor
import org.vitrivr.engine.core.model.descriptor.scalar.LongDescriptor
import org.vitrivr.engine.core.model.descriptor.vector.DoubleVectorDescriptor
import org.vitrivr.engine.core.model.descriptor.vector.FloatVectorDescriptor
import org.vitrivr.engine.core.model.descriptor.vector.IntVectorDescriptor
import org.vitrivr.engine.core.model.descriptor.vector.LongVectorDescriptor
import org.vitrivr.engine.core.model.metamodel.Analyser
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.query.Query
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.model.retrievable.attributes.DescriptorAuthorAttribute
import org.vitrivr.engine.core.model.types.Value
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.core.operators.ingest.Extractor
import org.vitrivr.engine.core.operators.retrieve.Retriever
import java.util.*
import kotlin.reflect.KClass

class DescriptorFieldMapper : Analyser<ContentElement<*>, Descriptor> {

    override val contentClasses: Set<KClass<out ContentElement<*>>> = emptySet() //no content is processed
    override val descriptorClass: KClass<Descriptor> = Descriptor::class

    companion object {
        const val TYPE_PARAMETER_NAME = "type"
        const val LENGTH_PARAMETER_NAME = "length"
        const val AUTHORNAME_PARAMETER_NAME = "authorName"
    }

    enum class DescriptorType {
        FLOAT,
        FLOAT_VECTOR,
        DOUBLE,
        DOUBLE_VECTOR,
        INT,
        INT_VECTOR;
    }

//    private inline fun <reified T : Descriptor> prototypeForField(field: Schema.Field<*, T>) : Descriptor {
//        return when (T::class) {
//            FloatDescriptor::class -> FloatDescriptor(UUID.randomUUID(), UUID.randomUUID(), Value.Float(0f))
//        }
//    }

    override fun prototype(field: Schema.Field<*, *>): Descriptor {
        val descriptorType = DescriptorType.valueOf(
            field.parameters[TYPE_PARAMETER_NAME]
                ?: throw IllegalArgumentException("'$TYPE_PARAMETER_NAME' is not defined")
        )
        return when (descriptorType) {
            DescriptorType.FLOAT -> FloatDescriptor(UUID.randomUUID(), UUID.randomUUID(), Value.Float(0f))
            DescriptorType.DOUBLE -> DoubleDescriptor(UUID.randomUUID(), UUID.randomUUID(), Value.Double(0.0))
            DescriptorType.INT -> IntDescriptor(UUID.randomUUID(), UUID.randomUUID(), Value.Int(0))
            DescriptorType.FLOAT_VECTOR -> FloatVectorDescriptor(
                UUID.randomUUID(),
                UUID.randomUUID(),
                Value.FloatVector(
                    field.parameters[LENGTH_PARAMETER_NAME]?.toIntOrNull()
                        ?: throw IllegalArgumentException("'$LENGTH_PARAMETER_NAME' is not defined")
                )
            )

            DescriptorType.DOUBLE_VECTOR -> DoubleVectorDescriptor(
                UUID.randomUUID(),
                UUID.randomUUID(),
                Value.DoubleVector(
                    field.parameters[LENGTH_PARAMETER_NAME]?.toIntOrNull()
                        ?: throw IllegalArgumentException("'$LENGTH_PARAMETER_NAME' is not defined")
                )
            )

            DescriptorType.INT_VECTOR -> IntVectorDescriptor(
                UUID.randomUUID(),
                UUID.randomUUID(),
                Value.IntVector(
                    field.parameters[LENGTH_PARAMETER_NAME]?.toIntOrNull()
                        ?: throw IllegalArgumentException("'$LENGTH_PARAMETER_NAME' is not defined")
                )
            )
        }
    }

    override fun newExtractor(
        field: Schema.Field<ContentElement<*>, Descriptor>,
        input: Operator<Retrievable>,
        context: IndexContext
    ): Extractor<ContentElement<*>, Descriptor> {
        val authorName = field.parameters[AUTHORNAME_PARAMETER_NAME]
            ?: throw IllegalArgumentException("'$AUTHORNAME_PARAMETER_NAME' is not defined")
        return Mapper(input, field, authorName)
    }

    inner class Mapper(
        override val input: Operator<out Retrievable>,
        override val field: Schema.Field<ContentElement<*>, Descriptor>,
        private val authorName: String
    ) : Extractor<ContentElement<*>, Descriptor> {
        override val analyser = this@DescriptorFieldMapper
        override val name = field.fieldName
        override val persisting = true

        private val descriptorType = DescriptorType.valueOf(
            field.parameters[TYPE_PARAMETER_NAME]
                ?: throw IllegalArgumentException("'$TYPE_PARAMETER_NAME' is not defined")
        )

        override fun toFlow(scope: CoroutineScope) = this.input.toFlow(scope).onEach { retrievable ->

            val ids = retrievable.filteredAttribute(DescriptorAuthorAttribute::class.java)?.getDescriptorIds(authorName) ?: return@onEach
            val descriptors = retrievable.descriptors.filter { it.id in ids }

            if (descriptors.isEmpty()) {
                return@onEach
            }

            val typeChecked = descriptors.map {
                when (descriptorType) {
                    DescriptorType.FLOAT -> toFloatDescriptor(it)
                    DescriptorType.FLOAT_VECTOR -> toFloatVectorDescriptor(it)
                    DescriptorType.DOUBLE -> toDoubleDescriptor(it)
                    DescriptorType.DOUBLE_VECTOR -> toDoubleVectorDescriptor(it)
                    DescriptorType.INT -> toIntDescriptor(it)
                    DescriptorType.INT_VECTOR -> toIntVectorDescriptor(it)
                }
            }

            descriptors.forEach {
                retrievable.removeDescriptor(it)
            }

            val authorAttribute = DescriptorAuthorAttribute()

            typeChecked.forEach {
                retrievable.addDescriptor(it)
                authorAttribute.add(it, this.name)
            }

            retrievable.addAttribute(authorAttribute)

        }

        private fun toFloatDescriptor(descriptor: Descriptor): FloatDescriptor {
            field as Schema.Field<*, FloatDescriptor>
            return when(descriptor) {
                is FloatDescriptor -> descriptor.copy(field = field)
                is DoubleDescriptor -> FloatDescriptor(descriptor.id, descriptor.retrievableId, Value.Float(descriptor.value.value.toFloat()), field)
                is IntDescriptor -> FloatDescriptor(descriptor.id, descriptor.retrievableId, Value.Float(descriptor.value.value.toFloat()), field)
                is LongDescriptor -> FloatDescriptor(descriptor.id, descriptor.retrievableId, Value.Float(descriptor.value.value.toFloat()), field)
                else -> throw IllegalArgumentException("${descriptor::class.simpleName} cannot be transformed to FloatDescriptor")
            }
        }

        private fun toDoubleDescriptor(descriptor: Descriptor): DoubleDescriptor {
            field as Schema.Field<*, DoubleDescriptor>
            return when(descriptor) {
                is FloatDescriptor -> DoubleDescriptor(descriptor.id, descriptor.retrievableId, Value.Double(descriptor.value.value.toDouble()), field)
                is DoubleDescriptor -> descriptor.copy(field = field)
                is IntDescriptor -> DoubleDescriptor(descriptor.id, descriptor.retrievableId, Value.Double(descriptor.value.value.toDouble()), field)
                is LongDescriptor -> DoubleDescriptor(descriptor.id, descriptor.retrievableId, Value.Double(descriptor.value.value.toDouble()), field)
                else -> throw IllegalArgumentException("${descriptor::class.simpleName} cannot be transformed to DoubleDescriptor")
            }
        }

        private fun toIntDescriptor(descriptor: Descriptor): IntDescriptor {
            field as Schema.Field<*, IntDescriptor>
            return when(descriptor) {
                is FloatDescriptor -> IntDescriptor(descriptor.id, descriptor.retrievableId, Value.Int(descriptor.value.value.toInt()), field)
                is DoubleDescriptor -> IntDescriptor(descriptor.id, descriptor.retrievableId, Value.Int(descriptor.value.value.toInt()), field)
                is IntDescriptor -> descriptor.copy(field = field)
                is LongDescriptor -> IntDescriptor(descriptor.id, descriptor.retrievableId, Value.Int(descriptor.value.value.toInt()), field)
                else -> throw IllegalArgumentException("${descriptor::class.simpleName} cannot be transformed to IntDescriptor")
            }
        }

        private fun toFloatVectorDescriptor(descriptor: Descriptor): FloatVectorDescriptor {
            field as Schema.Field<*, FloatVectorDescriptor>
            return when(descriptor) {
                is FloatVectorDescriptor -> descriptor.copy(field = field)
                is DoubleVectorDescriptor -> FloatVectorDescriptor(descriptor.id, descriptor.retrievableId, Value.FloatVector(FloatArray(descriptor.vector.size){descriptor.vector.value[it].toFloat()}), field)
                is IntVectorDescriptor -> FloatVectorDescriptor(descriptor.id, descriptor.retrievableId, Value.FloatVector(FloatArray(descriptor.vector.size){descriptor.vector.value[it].toFloat()}), field)
                is LongVectorDescriptor -> FloatVectorDescriptor(descriptor.id, descriptor.retrievableId, Value.FloatVector(FloatArray(descriptor.vector.size){descriptor.vector.value[it].toFloat()}), field)
                else -> throw IllegalArgumentException("${descriptor::class.simpleName} cannot be transformed to FloatVectorDescriptor")
            }
        }

        private fun toDoubleVectorDescriptor(descriptor: Descriptor): DoubleVectorDescriptor {
            field as Schema.Field<*, DoubleVectorDescriptor>
            return when(descriptor) {
                is FloatVectorDescriptor -> DoubleVectorDescriptor(descriptor.id, descriptor.retrievableId, Value.DoubleVector(DoubleArray(descriptor.vector.size){descriptor.vector.value[it].toDouble()}), field)
                is DoubleVectorDescriptor -> descriptor.copy(field = field)
                is IntVectorDescriptor -> DoubleVectorDescriptor(descriptor.id, descriptor.retrievableId, Value.DoubleVector(DoubleArray(descriptor.vector.size){descriptor.vector.value[it].toDouble()}), field)
                is LongVectorDescriptor -> DoubleVectorDescriptor(descriptor.id, descriptor.retrievableId, Value.DoubleVector(DoubleArray(descriptor.vector.size){descriptor.vector.value[it].toDouble()}), field)
                else -> throw IllegalArgumentException("${descriptor::class.simpleName} cannot be transformed to DoubleVectorDescriptor")
            }
        }

        private fun toIntVectorDescriptor(descriptor: Descriptor): IntVectorDescriptor {
            field as Schema.Field<*, IntVectorDescriptor>
            return when(descriptor) {
                is FloatVectorDescriptor -> IntVectorDescriptor(descriptor.id, descriptor.retrievableId, Value.IntVector(IntArray(descriptor.vector.size){descriptor.vector.value[it].toInt()}), field)
                is DoubleVectorDescriptor -> IntVectorDescriptor(descriptor.id, descriptor.retrievableId, Value.IntVector(IntArray(descriptor.vector.size){descriptor.vector.value[it].toInt()}), field)
                is IntVectorDescriptor -> descriptor.copy(field = field)
                is LongVectorDescriptor -> IntVectorDescriptor(descriptor.id, descriptor.retrievableId, Value.IntVector(IntArray(descriptor.vector.size){descriptor.vector.value[it].toInt()}), field)
                else -> throw IllegalArgumentException("${descriptor::class.simpleName} cannot be transformed to IntVectorDescriptor")
            }
        }


    }

    override fun newExtractor(
        name: String,
        input: Operator<Retrievable>,
        context: IndexContext
    ): Extractor<ContentElement<*>, Descriptor> {
        throw UnsupportedOperationException("DescriptorPersister required backing field")
    }

    override fun newRetrieverForQuery(
        field: Schema.Field<ContentElement<*>, Descriptor>,
        query: Query,
        context: QueryContext
    ): Retriever<ContentElement<*>, Descriptor> {
        throw UnsupportedOperationException("DescriptorPersister does not support retrieval")
    }

    override fun newRetrieverForContent(
        field: Schema.Field<ContentElement<*>, Descriptor>,
        content: Collection<ContentElement<*>>,
        context: QueryContext
    ): Retriever<ContentElement<*>, Descriptor> {
        throw UnsupportedOperationException("DescriptorPersister does not support retrieval")
    }

    override fun newRetrieverForDescriptors(
        field: Schema.Field<ContentElement<*>, Descriptor>,
        descriptors: Collection<Descriptor>,
        context: QueryContext
    ): Retriever<ContentElement<*>, Descriptor> {
        throw UnsupportedOperationException("DescriptorPersister does not support retrieval")
    }
}