package org.vitrivr.engine.core.model.query.bool

/**
 * Represents a logical AND combination of multiple [BooleanQuery] clauses.
 *
 * All [BooleanQuery] instances provided in the [clauses] list must evaluate to true
 * for this [AndBooleanQuery] to be considered true by the query engine.
 *
 * @property clauses The list of [BooleanQuery] instances to be connected via AND. Must contain at least two clauses.
 * @property limit The maximum number of results that should be returned by this [AndBooleanQuery] after all conditions are applied.
 * @author henrikluemkemann
 * @version 1.0.0
 */
data class AndBooleanQuery(val clauses: List<BooleanQuery>, override val limit: Long = Long.MAX_VALUE) : BooleanQuery {

    init {
        require(clauses.size >= 2) {
            "An AndBooleanQuery must contain at least two clauses. For a single clause, use a SimpleBooleanQuery instead."
        }
    }

    /**
     * Convenience constructor to create an [AndBooleanQuery] from a variable number of [BooleanQuery] arguments.
     *
     * @param clauses Vararg array of [BooleanQuery] instances.
     * @param limit The maximum number of results for this query.
     */
    constructor(vararg clauses: BooleanQuery, limit: Long = Long.MAX_VALUE) : this(clauses.toList(), limit)
}