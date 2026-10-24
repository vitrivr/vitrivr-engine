package org.vitrivr.engine.query.aggregate

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.toList
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.model.retrievable.attributes.ScoreAttribute
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.core.operators.general.Aggregator

/**
 * Intersects inputs by retrievable ID, emitting each shared ID once in first-input order.
 * Duplicate IDs within an input count only once (the first occurrence is used).
 * Scores are the minimum across inputs, with absent scores treated as zero.
 * Content, descriptors and other attributes come from the first input; relationships are combined.
 */
class And(
    override val inputs: List<Operator<out Retrievable>>,
    override val name: String
) : Aggregator {
    init {
        require(inputs.isNotEmpty()) { "And requires at least one input." }
    }

    override fun toFlow(scope: CoroutineScope): Flow<Retrievable> = flow {
        val candidates = inputs.first().toFlow(scope).toList().distinctBy { it.id }
            .associateByTo(linkedMapOf()) { it.id }
        for (input in inputs.drop(1)) {
            if (candidates.isEmpty()) break
            val matches = input.toFlow(scope).toList().distinctBy { it.id }.associateBy { it.id }
            val iterator = candidates.iterator()
            while (iterator.hasNext()) {
                val entry = iterator.next()
                val match = matches[entry.key]
                if (match == null) {
                    iterator.remove()
                } else {
                    val first = entry.value
                    val score = minOf(
                        first.filteredAttribute(ScoreAttribute::class.java)?.score ?: 0.0,
                        match.filteredAttribute(ScoreAttribute::class.java)?.score ?: 0.0
                    )
                    entry.setValue(first.copy(
                        attributes = first.attributes.filterNot { it is ScoreAttribute }.toSet() + ScoreAttribute.Unbound(score),
                        relationships = first.relationships + match.relationships
                    ))
                }
            }
        }
        candidates.values.forEach { emit(it) }
    }
}
