package org.vitrivr.engine.core.operators.transform.shape

import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.operators.Operator

/**
 * Types of merging operations that can be performed by the [MergeOperator].
 *
 * @author Ralph Gasser
 * @version 1.1.0
 */
enum class MergeType {
    MERGE, COMBINE, CONCAT;


    /**
     * Generates the [Operator] that corresponds to the [MergeType].
     *
     * @param inputs A [List] of input [Operator]s.
     */
    fun operator(inputs: List<Operator<Retrievable>>) = when (this) {
        MERGE -> MergeOperator(inputs)
        COMBINE -> CombineOperator(inputs)
        CONCAT -> ConcatOperator(inputs)
    }
}
