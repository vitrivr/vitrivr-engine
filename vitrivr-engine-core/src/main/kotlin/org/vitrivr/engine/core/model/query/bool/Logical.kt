package org.vitrivr.engine.core.model.query.bool

/**
 *
 * @author Ralph Gasser
 * @version 1.0
 */
sealed interface Logical : BooleanPredicate {
    data class And(val predicates: List<BooleanPredicate>) : Logical

    data class Or(val predicates: List<BooleanPredicate>) : Logical
}