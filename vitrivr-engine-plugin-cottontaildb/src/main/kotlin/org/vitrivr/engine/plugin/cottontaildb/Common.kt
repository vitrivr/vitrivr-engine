package org.vitrivr.engine.plugin.cottontaildb

import org.vitrivr.cottontail.client.language.basics.predicate.Compare
import org.vitrivr.cottontail.core.types.Types
import org.vitrivr.cottontail.core.values.*
import org.vitrivr.engine.core.model.descriptor.FieldType
import org.vitrivr.engine.core.model.descriptor.scalar.*
import org.vitrivr.engine.core.model.descriptor.vector.*
import org.vitrivr.engine.core.model.query.basics.ComparisonOperator
import org.vitrivr.engine.core.model.query.bool.ScalarBooleanQuery
import org.vitrivr.engine.core.model.query.bool.SimpleBooleanQuery
import org.vitrivr.engine.core.model.query.bool.StructSimpleBooleanQuery

/** The name of the retrievable entity. */
const val RETRIEVABLE_ENTITY_NAME = "retrievable"

/** The column name of a retrievable ID. */
const val RETRIEVABLE_ID_COLUMN_NAME = "retrievableId"

/** The column name of a retrievable ID. */
const val RETRIEVABLE_TYPE_COLUMN_NAME = "type"

/** The name of the retrievable entity. */
const val RELATIONSHIP_ENTITY_NAME = "relationships"

/** The column name of a retrievable ID. */
const val SUBJECT_ID_COLUMN_NAME = "subjectId"

/** The column name of a retrievable ID. */
const val OBJECT_ID_COLUMN_NAME = "objectId"

/** The column name of a retrievable ID. */
const val PREDICATE_COLUMN_NAME = "predicate"

/** The prefix for descriptor entities. */
const val DESCRIPTOR_ENTITY_PREFIX = "descriptor"

/** The column name of a descriptor ID. */
const val DESCRIPTOR_ID_COLUMN_NAME = "descriptorId"

/** The column name of a descriptor ID. */
const val DESCRIPTOR_COLUMN_NAME = "descriptor"

/** The column name used to describe a distance.*/
const val DISTANCE_COLUMN_NAME = "distance"

/** The column name used to describe a distance.*/
const val SCORE_COLUMN_NAME = "score"

/**
 * Extracts the [Compare.Operator] from this [ScalarBooleanQuery].
 *
 * @return [Compare.Operator] used for this [ScalarBooleanQuery]
 */
internal fun SimpleBooleanQuery<*>.operator() = when (this.comparison) {
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

internal fun StructSimpleBooleanQuery<*,*>.toValue(): PublicValue = when(this.fieldAndValueType){
    FieldType.STRING -> StringValue(this.value as String)
    FieldType.BOOLEAN -> BooleanValue(this.value as Boolean)
    FieldType.BYTE -> ByteValue(this.value as Byte)
    FieldType.SHORT -> ShortValue(this.value as Short)
    FieldType.INT -> IntValue(this.value as Int)
    FieldType.LONG -> LongValue(this.value as Long)
    FieldType.FLOAT -> FloatValue(this.value as Float)
    FieldType.DOUBLE -> DoubleValue(this.value as Double)
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

