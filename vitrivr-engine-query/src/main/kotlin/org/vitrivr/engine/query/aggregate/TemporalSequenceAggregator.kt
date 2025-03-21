package org.vitrivr.engine.query.aggregate

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.toList
import org.vitrivr.engine.core.model.relationship.Relationship
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.model.retrievable.RetrievableId
import org.vitrivr.engine.core.model.retrievable.Retrieved
import org.vitrivr.engine.core.model.retrievable.attributes.PropertyAttribute
import org.vitrivr.engine.core.model.retrievable.attributes.ScoreAttribute
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.core.operators.general.Aggregator
import java.util.*

class TemporalSequenceAggregator(
    override val inputs: List<Operator<out Retrievable>>, override val name: String
) : Aggregator {

    companion object {
        const val PADDING_TIME = 1_000_000_000 //1 second in ns
        const val MAX_TIME_BETWEEN_STAGES = 10_000_000_000 //10 second in ns
    }

    data class ContinuousSequence(
        val retrieved: List<Retrievable>,
        val start: Long,
        val end: Long,
        val score: Double,
        val stage: Int
    )

    override fun toFlow(scope: CoroutineScope): Flow<Retrievable> = flow {

        val inputs = inputs.map { it.toFlow(scope).toList() }

        //pass along results
        inputs.asSequence().flatten().forEach {
            emit(it)
        }

        //at least 2 inputs are required for a sequence
        if (inputs.size < 2 || inputs.filter { it.isNotEmpty() }.size < 2) {
            return@flow
        }

        //start with temporal aggregation

        val continuousSequences = mutableMapOf<RetrievableId, MutableList<ContinuousSequence>>()

        for ((stageCounter, stage) in inputs.withIndex()) {

            val retrievedMap = stage.associateBy { it.id }

            val sources = stage.filter { it.type == "source" }

            for (source in sources) {
                val relationships = source.relationships
                if (relationships.isEmpty()) continue

                //get all valid segments per source, sorted by time if available
                val segments = relationships.asSequence().filter {
                    it.predicate == "partOf" && it.objectId == source.id
                }.mapNotNull {
                    retrievedMap[it.subjectId]
                }.map {
                    val properties = it.filteredAttribute(PropertyAttribute::class.java)?.properties ?: emptyMap()
                    it to properties
                }.filter {
                    it.second["start"]?.toLongOrNull() != null && it.second["end"]?.toLongOrNull() != null
                }.sortedBy {
                    it.second["start"]!!.toLong()
                }.map { it.first }.toList()

                if (segments.isEmpty()) {
                    continue
                }

                val sequences = mutableListOf<MutableList<Retrievable>>()
                var currentSequence = mutableListOf<Retrievable>()
                var lastEndTime = -1L

                for (segment in segments) {

                    val properties = segment.filteredAttribute(PropertyAttribute::class.java)!!.properties

                    val startTime = properties["start"]!!.toLong()

                    //if gap between end of last sequence and start of current segment is larger than padding, start new sequence
                    if ((lastEndTime + PADDING_TIME) > startTime) {
                        if (currentSequence.isNotEmpty()) {
                            sequences.add(currentSequence)
                            currentSequence = mutableListOf()
                        }
                    }

                    currentSequence.add(segment)

                    lastEndTime = properties["end"]!!.toLong()
                }

                if (currentSequence.isNotEmpty()) {
                    sequences.add(currentSequence)
                }

                if (sequences.isEmpty()) {
                    continue
                }

                if (!continuousSequences.containsKey(source.id)) {
                    continuousSequences[source.id] = mutableListOf()
                }

                for (sequence in sequences) {

                    val start = sequence.first()
                        .filteredAttribute(PropertyAttribute::class.java)!!.properties["start"]!!.toLong()
                    val end =
                        sequence.last().filteredAttribute(PropertyAttribute::class.java)!!.properties["end"]!!.toLong()
                    val score =
                        sequence.maxOfOrNull { (it.filteredAttribute(ScoreAttribute::class.java))?.score ?: 0.0 } ?: 0.0

                    continuousSequences[source.id]!!.add(
                        ContinuousSequence(
                            sequence, start, end, score, stageCounter
                        )
                    )

                }

            }

        }

        //found all candidates, now construct

        val temporalSequences = mutableListOf<List<ContinuousSequence>>()

        for ((_, sequences) in continuousSequences) {

            val stages = sequences.groupBy { it.stage }

            //skip sequences that have only results for one 'stage'
            if (stages.size < 2) {
                continue
            }

            //sequentially go over all stage indices to try and start sequences
            for (startStageId in inputs.indices) {

                for (startSequence in stages[startStageId] ?: emptyList()) {

                    val temporalSequence = mutableListOf(startSequence)

                    //find best match from next stage to grow sequence
                    for (nextStageId in ((startStageId + 1) until inputs.size)) {

                        val maxStartTime = temporalSequence.last().end + MAX_TIME_BETWEEN_STAGES
                        val minStartTime = temporalSequence.last().start

                        stages[nextStageId]?.filter { it.start in minStartTime..maxStartTime }?.maxByOrNull { it.score }
                            ?.let {
                                temporalSequence.add(it) //add highest scored sequence within range
                            }

                    }

                    //if length is at least 2 and not a true subsequence of an existing one, add it
                    if (temporalSequence.size > 1 && !temporalSequences.any { existingSequence ->
                            temporalSequence.all { existingSequence.contains(it) }
                        }) {
                        temporalSequences.add(temporalSequence)
                    }

                }

            }

        }

        //transform remaining for emission

        temporalSequences.forEach { sequence ->
            val score = sequence.maxOf { it.score }
            val id = UUID.randomUUID()

            val relationships = sequence.flatMap {
                it.retrieved.map { r -> Relationship.ById(r.id, "partOf", id, false) }
            }.toSet()

            if (relationships.size < 2) {
                return@forEach
            }
            emit(Retrieved(id, "temporalSequence", attributes = setOf(ScoreAttribute.Unbound(score)), relationships = relationships, transient = true))
        }

    }
}