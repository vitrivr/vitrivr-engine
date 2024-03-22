package org.vitrivr.engine.core.model.query.basics

import org.vitrivr.engine.core.model.query.bool.SimpleBooleanQuery

/**
 * Enumeration of comparison operators used by the [SimpleBooleanQuery].
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
enum class ComparisonOperator(val value: String) {
    EQ("="),
    NEQ("!="),
    LE("<"),
    GR(">"),
    LEQ("<="),
    GEQ(">=");
}