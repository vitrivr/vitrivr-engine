package org.vitrivr.engine.core.model.query.bool

import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.query.basics.ComparisonOperator
import org.vitrivr.engine.core.model.types.Value
import org.vitrivr.engine.core.model.types.Value.ScalarValue

/**
 * A [Comparison] that compares a field to a [Value] of type [T] using the given [ComparisonOperator].
 *
 *
 * @author Ralph Gasser
 * @version 2.0.0
 */
sealed interface Comparison<T : ScalarValue<*>> : BooleanPredicate {
    /** The [Schema.Field] being compared. */
    val field: Schema.Field<*, *>

    /**
     * The (optional) name of the attribute that should be compared.
     *
     * Typically, this is pre-determined by the analyser. However, in some cases, this must be specified (e.g., when querying struct fields).
     */
    val attributeName: String?

    /**
     * An [Equals] [Comparison] that compares a field to a [Value] of type [T] using the [ComparisonOperator.EQ] operator.
     */
    data class Equals<T : ScalarValue<*>>(override val field: Schema.Field<*, *>, override val attributeName: String?, val value: T) : Comparison<T>

    /**
     * An [NotEquals] [Comparison] that compares a field to a [Value] of type [T] using the [ComparisonOperator.EQ] operator.
     */
    data class NotEquals<T : ScalarValue<*>>(override val field: Schema.Field<*, *>, override val attributeName: String?, val value: T) : Comparison<T>

    /**
     * An [Greater] [Comparison] that compares a field to a [Value] of type [T] using the [ComparisonOperator.EQ] operator.
     */
    data class Greater<T : ScalarValue<*>>(override val field: Schema.Field<*, *>, override val attributeName: String?, val value: T) : Comparison<T>

    /**
     * An [GreaterEquals] [Comparison] that compares a field to a [Value] of type [T] using the [ComparisonOperator.EQ] operator.
     */
    data class GreaterEquals<T : ScalarValue<*>>(override val field: Schema.Field<*, *>, override val attributeName: String?, val value: T) : Comparison<T>

    /**
     * An [Less] [Comparison] that compares a field to a [Value] of type [T] using the [ComparisonOperator.EQ] operator.
     */
    data class Less<T : ScalarValue<*>>(override val field: Schema.Field<*, *>, override val attributeName: String?, val value: T) : Comparison<T>

    /**
     * An [LessEquals] [Comparison] that compares a field to a [Value] of type [T] using the [ComparisonOperator.EQ] operator.
     */
    data class LessEquals<T : ScalarValue<*>>(override val field: Schema.Field<*, *>, override val attributeName: String?, val value: T) : Comparison<T>

    /**
     * An [Like] [Comparison] that compares a field to a [Value] of type [T] using the [ComparisonOperator.EQ] operator.
     */
    data class Like<T : ScalarValue<String>>(override val field: Schema.Field<*, *>, override val attributeName: String?, val value: T) : Comparison<T>

    /**
     * An [In] [Comparison] that compares a field to a [Value] of type [T] using the [ComparisonOperator.EQ] operator.
     */
    data class In<T : ScalarValue<String>>(override val field: Schema.Field<*, *>, override val attributeName: String?, val values: List<T>) : Comparison<T>
}
