package org.vitrivr.engine.index.segment

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.ProducerScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import org.vitrivr.engine.core.context.Context
import org.vitrivr.engine.core.model.content.element.ContentElement
import org.vitrivr.engine.core.model.descriptor.Descriptor
import org.vitrivr.engine.core.model.descriptor.vector.DoubleVectorDescriptor
import org.vitrivr.engine.core.model.descriptor.vector.FloatVectorDescriptor
import org.vitrivr.engine.core.model.descriptor.vector.VectorDescriptor
import org.vitrivr.engine.core.model.query.basics.Distance
import org.vitrivr.engine.core.model.relationship.Relationship
import org.vitrivr.engine.core.model.retrievable.Ingested
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.model.retrievable.attributes.DescriptorAuthorAttribute
import org.vitrivr.engine.core.model.retrievable.attributes.RetrievableAttribute
import org.vitrivr.engine.core.model.retrievable.attributes.SourceAttribute
import org.vitrivr.engine.core.model.retrievable.attributes.time.TimeRangeAttribute
import org.vitrivr.engine.core.model.types.Value
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.core.operators.general.Transformer
import org.vitrivr.engine.core.operators.general.TransformerFactory
import org.vitrivr.engine.core.source.Source
import java.util.*

/**
 * Segments a stream of [Ingested] based on [Distance] between [VectorDescriptor]s from a specific author.
 */
class DescriptorDistanceSegmenter : TransformerFactory {

    override fun newTransformer(name: String, input: Operator<out Retrievable>, context: Context): Transformer {

        val distance = Distance.valueOf(context[name, "distance"] ?: throw IllegalArgumentException("Property 'distance' must be specified"))
        val authorName = context[name, "authorName"] ?: throw IllegalArgumentException("Property 'authorName' must be specified")
        val atMost = context[name, "atMost"]?.toDoubleOrNull()
        val atLeast = context[name, "atLeast"]?.toDoubleOrNull()

        if (atMost == null && atLeast == null) {
            throw IllegalArgumentException("Property 'atLeast' or 'atMost' must be specified.")
        }

        return Instance(input, authorName, distance, atLeast ?: Double.NEGATIVE_INFINITY, atMost ?: Double.POSITIVE_INFINITY, name)
    }

    private class Instance(
        override val input: Operator<out Retrievable>,
        private val authorName: String,
        private val distance: Distance,
        private val atLeast: Double,
        private val atMost: Double,
        override val name: String
    ) : Transformer {

        private fun compare(comparisonAnchor: Value.Vector<*>, descriptor: VectorDescriptor<*, *>): Boolean {
            val dist = when (comparisonAnchor) {
                is Value.FloatVector -> distance(comparisonAnchor, (descriptor as FloatVectorDescriptor).vector)
                is Value.DoubleVector -> distance(comparisonAnchor, (descriptor as DoubleVectorDescriptor).vector)
                else -> throw UnsupportedOperationException("Unsupported value type ${comparisonAnchor::class.java.name}")
            }
            return dist in atLeast..atMost
        }


        override fun toFlow(scope: CoroutineScope): Flow<Retrievable> = channelFlow {

            val cache = mutableListOf<Retrievable>()
            var comparisonAnchor: Value.Vector<*>? = null
            var lastSource: Source? = null

            this@Instance.input.toFlow(scope).collect { ingested ->


                if(ingested.type !== "SEGMENT") {
                    if (cache.isNotEmpty()) {
                        send(this, cache)
                        cache.clear()
                    }
                    send(ingested)
                    return@collect
                }

                val source = ingested.filteredAttribute(SourceAttribute::class.java)?.source ?: return@collect

                /* Check if source has changed. */
                if (lastSource != source) {
                    if (cache.isNotEmpty()) {
                        send(this, cache)
                        cache.clear()
                    }
                    comparisonAnchor = null
                    lastSource = source
                }

                val descriptorIds =
                    ingested.filteredAttribute(DescriptorAuthorAttribute::class.java)?.getDescriptorIds(authorName)
                        ?: emptySet()
                val descriptors =
                    ingested.descriptors.filter { it.id in descriptorIds }.filterIsInstance<VectorDescriptor<*, *>>()
                if (descriptors.isEmpty()) {
                    return@collect
                }

                //if no anchor is set yet, set and return
                if (comparisonAnchor == null) {
                    cache.add(ingested)
                    comparisonAnchor = descriptors.first().vector
                    return@collect
                }

                //otherwise compare to previous anchor
                if (descriptors.all { compare(comparisonAnchor!!, it) }) {
                    cache.add(ingested)
                } else { //if not within range, emit new segment
                    send(this, cache)
                    cache.clear()
                    comparisonAnchor = descriptors.first{!compare(comparisonAnchor!!, it)}.vector
                    cache.add(ingested)
                }


            }

            if(cache.isNotEmpty()) {
                send(this, cache)
            }
        }

        private suspend fun send(
            downstream: ProducerScope<Retrievable>,
            cache: List<Retrievable>
        ) {
            val retrievableId = UUID.randomUUID()
            val content = mutableListOf<ContentElement<*>>()
            val descriptors = mutableSetOf<Descriptor<*>>()
            val attributes = mutableSetOf<RetrievableAttribute>()
            val relationships = mutableSetOf<Relationship>()

            val ranges = mutableSetOf<TimeRangeAttribute>()
            for (emitted in cache) {
                emitted.content.forEach { content.add(it) }
                emitted.descriptors.forEach { descriptors.add(it) }
                emitted.attributes.forEach {
                    if (it is TimeRangeAttribute) {
                        ranges.add(it)
                    } else {
                        attributes.add(it)
                    }
                }
                emitted.relationships.forEach { relationship ->
                    /*
                     * TODO @lucaro:
                     * relationships.add(relationship.exchange(emitted.id, retrievableId))
                     */
                }
            }
            if (ranges.isNotEmpty()) {
                attributes.add(TimeRangeAttribute.merge(ranges))
            }

            /* Send retrievable downstream. */
            downstream.send(Ingested(retrievableId, cache.first().type,  content, descriptors, attributes, relationships, false))
        }

    }
}