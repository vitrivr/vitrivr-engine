package org.vitrivr.engine.core.model.query.basics

import org.vitrivr.engine.core.model.query.bool.ScalarBooleanQuery

/**
 * Enumeration of comparison operators used by the [ScalarBooleanQuery].
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
