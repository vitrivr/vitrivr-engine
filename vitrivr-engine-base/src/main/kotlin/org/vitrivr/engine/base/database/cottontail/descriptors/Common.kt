package org.vitrivr.engine.base.database.cottontail.descriptors

import org.vitrivr.cottontail.client.language.basics.predicate.Compare
import org.vitrivr.cottontail.core.types.Types
import org.vitrivr.cottontail.core.values.*
import org.vitrivr.engine.core.model.descriptor.scalar.*
import org.vitrivr.engine.core.model.descriptor.vector.*
import org.vitrivr.engine.core.model.query.basics.ComparisonOperator
import org.vitrivr.engine.core.model.query.bool.BooleanQuery

/** The column name used to describe a feature.*/
const val DESCRIPTOR_COLUMN_NAME = "descriptor"


/** The column name used to describe a distance.*/
const val DISTANCE_COLUMN_NAME = "distance"

/**
 * Extracts the [Compare.Operator] from this [BooleanQuery].
 *
 * @return [Compare.Operator] used for this [BooleanQuery]
 */
internal fun BooleanQuery<ScalarDescriptor<*>>.operator() = when (this.comparison) {
    ComparisonOperator.EQ -> Compare.Operator.EQUAL
    ComparisonOperator.NEQ -> Compare.Operator.NOTEQUAL
    ComparisonOperator.LE -> Compare.Operator.LESS
    ComparisonOperator.GR -> Compare.Operator.GREATER
    ComparisonOperator.LEQ -> Compare.Operator.LEQUAL
    ComparisonOperator.GEQ -> Compare.Operator.GEQUAL
}

/**
 * Converts this [ScalarDescriptor] to a [PublicValue].
 *
 * @return [PublicValue] for this [ScalarDescriptor]
 */
internal fun ScalarDescriptor<*>.toValue(): PublicValue = when (this) {
    is BooleanDescriptor -> BooleanValue(this.value)
    is DoubleDescriptor -> DoubleValue(this.value)
    is FloatDescriptor -> FloatValue(this.value)
    is IntDescriptor -> IntValue(this.value)
    is LongDescriptor -> LongValue(this.value)
    is StringDescriptor -> StringValue(this.value)
}

/**
 * Converts this [ScalarDescriptor] to a [Types].
 *
 * @return [Types] for this [ScalarDescriptor]
 */
internal fun ScalarDescriptor<*>.toType() = when (this) {
    is BooleanDescriptor -> Types.Boolean
    is IntDescriptor -> Types.Int
    is LongDescriptor -> Types.Long
    is FloatDescriptor -> Types.Float
    is DoubleDescriptor -> Types.Double
    is StringDescriptor -> Types.String
}

/**
 * Converts this [VectorDescriptor] to a [PublicValue].
 *
 * @return [PublicValue] for this [VectorDescriptor]
 */
internal fun VectorDescriptor<*>.toValue(): PublicValue = when (this) {
    is BooleanVectorDescriptor -> BooleanVectorValue(this.vector.toTypedArray())
    is IntVectorDescriptor -> IntVectorValue(this.vector)
    is LongVectorDescriptor -> LongVectorValue(this.vector)
    is FloatVectorDescriptor -> FloatVectorValue(this.vector)
    is DoubleVectorDescriptor -> DoubleVectorValue(this.vector)

}

/**
 * Converts this [VectorDescriptor] to a [Types].
 *
 * @return [Types] for this [VectorDescriptor]
 */
internal fun VectorDescriptor<*>.toType() = when (this) {
    is BooleanVectorDescriptor -> Types.BooleanVector(this.dimensionality)
    is IntVectorDescriptor -> Types.IntVector(this.dimensionality)
    is LongVectorDescriptor -> Types.LongVector(this.dimensionality)
    is FloatVectorDescriptor -> Types.FloatVector(this.dimensionality)
    is DoubleVectorDescriptor -> Types.DoubleVector(this.dimensionality)
}

