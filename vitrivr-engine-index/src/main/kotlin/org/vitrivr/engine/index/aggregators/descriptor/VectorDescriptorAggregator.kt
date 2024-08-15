package org.vitrivr.engine.index.aggregators.descriptor

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.vitrivr.engine.core.context.Context
import org.vitrivr.engine.core.model.descriptor.vector.*
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.model.retrievable.attributes.DescriptorAuthorAttribute
import org.vitrivr.engine.core.model.types.Value
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.core.operators.general.Transformer
import org.vitrivr.engine.core.operators.general.TransformerFactory
import java.util.*

/**
 * Aggregates [VectorDescriptor]s from a common author, in case there are multiple.
 */
class VectorDescriptorAggregator : TransformerFactory {

    enum class AggregationStrategy {
        FIRST {
            override fun aggregate(collection: Collection<VectorDescriptor<*>>): VectorDescriptor<*> =
                collection.first()
        },
        MEAN {
            override fun aggregate(collection: Collection<VectorDescriptor<*>>): VectorDescriptor<*> {

                val vec = FloatArray(collection.first().vector.size)

                collection.forEach { descriptor ->
                    when (descriptor) {
                        is FloatVectorDescriptor -> {
                            for (i in vec.indices) {
                                vec[i] += descriptor.vector.value[i]
                            }
                        }

                        is DoubleVectorDescriptor -> {
                            for (i in vec.indices) {
                                vec[i] += descriptor.vector.value[i].toFloat()
                            }
                        }

                        is IntVectorDescriptor -> {
                            for (i in vec.indices) {
                                vec[i] += descriptor.vector.value[i].toFloat()
                            }
                        }

                        is LongVectorDescriptor -> {
                            for (i in vec.indices) {
                                vec[i] += descriptor.vector.value[i].toFloat()
                            }
                        }

                        is BooleanVectorDescriptor -> {
                            for (i in vec.indices) {
                                vec[i] += if (descriptor.vector.value[i]) 1f else 0f
                            }
                        }
                    }
                }

                val div = collection.size.toFloat()

                for (i in vec.indices) {
                    vec[i] /= div
                }

                return FloatVectorDescriptor(
                    UUID.randomUUID(), null, Value.FloatVector(vec)
                )
            }
        };

        abstract fun aggregate(collection: Collection<VectorDescriptor<*>>): VectorDescriptor<*>

    }

    override fun newTransformer(name: String, input: Operator<out Retrievable>, context: Context): Transformer {

        val authorName =
            context[name, "authorName"] ?: throw IllegalArgumentException("Property 'authorName' must be specified")
        val strategy = AggregationStrategy.valueOf(
            context[name, "strategy"] ?: throw IllegalArgumentException("Property 'strategy' must be specified")
        )

        return Instance(input, name, authorName, strategy)
    }

    private class Instance(
        override val input: Operator<out Retrievable>,
        private val name: String,
        private val authorName: String,
        private val strategy: AggregationStrategy
    ) : Transformer {


        override fun toFlow(scope: CoroutineScope): Flow<Retrievable> = this.input.toFlow(scope).map { ingested ->

            val descriptorIds =
                ingested.filteredAttribute(DescriptorAuthorAttribute::class.java)?.getDescriptorIds(authorName)
                    ?: emptySet()
            val descriptors =
                ingested.descriptors.filter { it.id in descriptorIds }.filterIsInstance<VectorDescriptor<*>>()

            if (descriptors.isEmpty()) {
                return@map ingested //nothing to do
            }

            val aggregated = if (descriptors.size == 1) {
                descriptors.first()
            } else {
                strategy.aggregate(descriptors)
            }

            descriptors.forEach {
                ingested.removeDescriptor(it)
            }

            ingested.addDescriptor(aggregated)
            ingested.addAttribute(DescriptorAuthorAttribute(aggregated.id, this.name))

            ingested
        }

    }

}