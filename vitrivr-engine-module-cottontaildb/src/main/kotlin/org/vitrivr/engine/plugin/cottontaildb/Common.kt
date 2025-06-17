package org.vitrivr.engine.plugin.cottontaildb

import org.vitrivr.cottontail.client.language.basics.predicate.Compare
import org.vitrivr.cottontail.core.types.Types
import org.vitrivr.cottontail.core.values.*
import org.vitrivr.engine.core.model.descriptor.Attribute
import org.vitrivr.engine.core.model.descriptor.scalar.*
import org.vitrivr.engine.core.model.descriptor.vector.*
import org.vitrivr.engine.core.model.query.basics.ComparisonOperator
import org.vitrivr.engine.core.model.query.bool.SimpleBooleanQuery
import org.vitrivr.engine.core.model.types.Type
import org.vitrivr.engine.core.model.types.Value
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.*

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

/** The column name used to describe a distance.*/
const val DISTANCE_COLUMN_NAME = "distance"

/** The column name used to describe a distance.*/
const val SCORE_COLUMN_NAME = "score"

/**
 * Converts a vitrivr-engine [Attribute] to a Cottontail DB [Types].
 *
 * @return [Compare.Operator] used for this [SimpleBooleanQuery]
 */
internal fun Type.toCottontailType(): Types<*> = when (this) {
    Type.String -> Types.String
    Type.Text -> Types.String
    Type.Byte -> Types.Byte
    Type.Short -> Types.Short
    Type.Boolean -> Types.Boolean
    Type.Int -> Types.Int
    Type.Long -> Types.Long
    Type.Float -> Types.Float
    Type.Double -> Types.Double
    Type.Datetime -> Types.Date
    Type.UUID -> Types.Uuid
    Type.Geography -> Types.String
    is Type.BooleanVector -> Types.BooleanVector(this.dimensions)
    is Type.DoubleVector -> Types.DoubleVector(this.dimensions)
    is Type.FloatVector -> Types.FloatVector(this.dimensions)
    is Type.IntVector -> Types.IntVector(this.dimensions)
    is Type.LongVector -> Types.LongVector(this.dimensions)
}

/**
 * Extracts the [Compare.Operator] from this [SimpleBooleanQuery].
 *
 * @return [Compare.Operator] used for this [SimpleBooleanQuery]
 */
internal fun SimpleBooleanQuery<*>.operator() = when (this.comparison) {
    ComparisonOperator.EQ -> Compare.Operator.EQUAL
    ComparisonOperator.NEQ -> Compare.Operator.NOTEQUAL
    ComparisonOperator.LE -> Compare.Operator.LESS
    ComparisonOperator.GR -> Compare.Operator.GREATER
    ComparisonOperator.LEQ -> Compare.Operator.LEQUAL
    ComparisonOperator.GEQ -> Compare.Operator.GEQUAL
    ComparisonOperator.LIKE -> Compare.Operator.LIKE
}

/**
 * Tries to convert this [Any] to a [PublicValue] (Cottontail DB).
 *
 * @return [PublicValue] for this [Any]
 */
internal fun Any.toCottontailValue(): PublicValue = when (this)  {
    is UUID -> UuidValue(this)
    is String -> StringValue(this)
    is Boolean -> BooleanValue(this)
    is Byte -> ByteValue(this)
    is Short -> ShortValue(this)
    is Int -> IntValue(this)
    is Long -> LongValue(this)
    is Float -> FloatValue(this)
    is Double -> DoubleValue(this)

    is Date -> DateValue(this)
    is LocalDateTime -> DateValue(Date.from(this.toInstant(ZoneOffset.UTC)))

    is Value.Boolean -> BooleanValue(this.value)
    is Value.Byte -> ByteValue(this.value)
    is Value.Double -> DoubleValue(this.value)
    is Value.Float -> FloatValue(this.value)
    is Value.Int -> IntValue(this.value)
    is Value.Long -> LongValue(this.value)
    is Value.Short -> ShortValue(this.value)
    is Value.String -> StringValue(this.value)
    is Value.DateTime -> DateValue(Date.from(this.value.toInstant(ZoneOffset.UTC)))
    is Value.GeographyValue -> StringValue(this.wkt)

    else -> throw IllegalArgumentException("Unsupported type for vector value.")
}

/**
 * Converts this [Value] (vitrivr-engine) to a [PublicValue] (Cottontail DB).
 *
 * @return [PublicValue] for this [Value]
 */
internal fun Value<*>.toCottontailValue(): PublicValue = when (this) {
    is Value.Boolean -> BooleanValue(this.value)
    is Value.Byte -> ByteValue(this.value)
    is Value.Double -> DoubleValue(this.value)
    is Value.Float -> FloatValue(this.value)
    is Value.Int -> IntValue(this.value)
    is Value.Long -> LongValue(this.value)
    is Value.Short -> ShortValue(this.value)
    is Value.String -> StringValue(this.value)
    is Value.Text -> StringValue(this.value)
    is Value.DateTime -> DateValue(Date.from(this.value.toInstant(ZoneOffset.UTC)))
    is Value.UUIDValue -> UuidValue(this.value)
    is Value.GeographyValue -> StringValue(this.wkt)
    is Value.BooleanVector -> BooleanVectorValue(this.value)
    is Value.DoubleVector -> DoubleVectorValue(this.value)
    is Value.FloatVector -> FloatVectorValue(this.value)
    is Value.IntVector -> IntVectorValue(this.value)
    is Value.LongVector -> LongVectorValue(this.value)
}

/**
 * Converts this [ScalarDescriptor] to a [PublicValue].
 *
 * @return [PublicValue] for this [ScalarDescriptor]
 */
internal fun ScalarDescriptor<*, *>.toCottontailValue(): PublicValue = this.value.toCottontailValue()

/**
 * Converts this [ScalarDescriptor] to a [Types].
 *
 * @return [Types] for this [ScalarDescriptor]
 */
internal fun ScalarDescriptor<*, *>.toType() = when (this) {
    is BooleanDescriptor -> Types.Boolean
    is ByteDescriptor -> Types.Byte
    is ShortDescriptor -> Types.Short
    is IntDescriptor -> Types.Int
    is LongDescriptor -> Types.Long
    is FloatDescriptor -> Types.Float
    is DoubleDescriptor -> Types.Double
    is StringDescriptor -> Types.String
    is TextDescriptor -> Types.String
}

/**
 * Converts a [List] of [Value] [Value] (vitrivr-engine) to a [VectorValue] (Cottontail DB).
 *
 * @return [VectorValue] for this [Value]
 */
internal fun List<Value<*>>.toCottontailValue(): PublicValue {
    val first = this.first()
    return when (first) {
        is Value.Boolean -> BooleanVectorValue(BooleanArray(this.size) { (this[it] as Value.Boolean).value })
        is Value.Float -> FloatVectorValue(FloatArray(this.size) { (this[it] as Value.Float).value })
        is Value.Double -> DoubleVectorValue(DoubleArray(this.size) { (this[it] as Value.Double).value })
        is Value.Int -> IntVectorValue(IntArray(this.size) { (this[it] as Value.Int).value })
        is Value.Long -> LongVectorValue(LongArray(this.size) { (this[it] as Value.Long).value })
        else -> throw IllegalArgumentException("Unsupported type for vector value.")
    }
}

/**
 * Converts this [VectorDescriptor] to a [PublicValue].
 *
 * @return [PublicValue] for this [VectorDescriptor]
 */
internal fun VectorDescriptor<*, *>.toCottontailValue(): PublicValue = this.vector.toCottontailValue()

/**
 * Converts this [VectorDescriptor] to a [Types].
 *
 * @return [Types] for this [VectorDescriptor]
 */
internal fun VectorDescriptor<*, *>.toType() = when (this) {
    is BooleanVectorDescriptor -> Types.BooleanVector(this.dimensionality)
    is IntVectorDescriptor -> Types.IntVector(this.dimensionality)
    is LongVectorDescriptor -> Types.LongVector(this.dimensionality)
    is FloatVectorDescriptor -> Types.FloatVector(this.dimensionality)
    is DoubleVectorDescriptor -> Types.DoubleVector(this.dimensionality)
}

