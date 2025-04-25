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
 * Aggregates [VectorDescriptor]s in case there are multiple.
 *
 * @author Luca Rossetto
 * @version 1.0.0
 */
class VectorDescriptorAggregator : TransformerFactory {

    enum class AggregationStrategy {
        FIRST {
            override fun aggregate(collection: Collection<VectorDescriptor<*, *>>): VectorDescriptor<*, *> =
                collection.first()
        },
        MEAN {
            override fun aggregate(collection: Collection<VectorDescriptor<*, *>>): VectorDescriptor<*, *> {

                val vec = DoubleArray(collection.first().vector.size)

                collection.forEach { descriptor ->
                    when (descriptor) {
                        is FloatVectorDescriptor -> {
                            for (i in vec.indices) {
                                vec[i] += descriptor.vector.value[i].toDouble()
                            }
                        }

                        is DoubleVectorDescriptor -> {
                            for (i in vec.indices) {
                                vec[i] += descriptor.vector.value[i]
                            }
                        }

                        is IntVectorDescriptor -> {
                            for (i in vec.indices) {
                                vec[i] += descriptor.vector.value[i].toDouble()
                            }
                        }

                        is LongVectorDescriptor -> {
                            for (i in vec.indices) {
                                vec[i] += descriptor.vector.value[i].toDouble()
                            }
                        }

                        is BooleanVectorDescriptor -> {
                            for (i in vec.indices) {
                                vec[i] += if (descriptor.vector.value[i]) 1.0 else 0.0
                            }
                        }
                    }
                }

                val div = collection.size.toDouble()

                for (i in vec.indices) {
                    vec[i] /= div
                }

                return DoubleVectorDescriptor(
                    UUID.randomUUID(), null, Value.DoubleVector(vec)
                )
            }
        };

        abstract fun aggregate(collection: Collection<VectorDescriptor<*, *>>): VectorDescriptor<*, *>

    }

    override fun newTransformer(name: String, input: Operator<out Retrievable>, context: Context): Transformer {

        val authorName =
            context[name, "authorName"] ?: throw IllegalArgumentException("Property 'authorName' must be specified")
        val strategy = AggregationStrategy.valueOf(
            context[name, "strategy"]?.uppercase() ?: throw IllegalArgumentException("Property 'strategy' must be specified")
        )

        return Instance(input, name, authorName, strategy)
    }

    private class Instance(
        override val input: Operator<out Retrievable>,
        override val name: String,
        private val authorName: String,
        private val strategy: AggregationStrategy
    ) : Transformer {

        override fun toFlow(scope: CoroutineScope): Flow<Retrievable> = this.input.toFlow(scope).map { ingested ->
            val descriptorIds = ingested.filteredAttribute(DescriptorAuthorAttribute::class.java)?.getDescriptorIds(authorName) ?: emptySet()
            val newDescriptors = ingested.descriptors.toMutableSet()
            val toAggregate = ingested.descriptors.filter { it.id in descriptorIds }.filterIsInstance<VectorDescriptor<*, *>>()

            if (toAggregate.isEmpty()) {
                return@map ingested //nothing to do
            }

            /* Perform aggregation. */
            val aggregated = if (toAggregate.size == 1) {
                toAggregate.first()
            } else {
                strategy.aggregate(toAggregate)
            }
            newDescriptors.add(aggregated)

            /* Remove the descriptors that were aggregated. */
            newDescriptors.removeIf { toAggregate.contains(it) }

            /* Return copy. */
            ingested.copy(descriptors = newDescriptors)
        }
    }
}