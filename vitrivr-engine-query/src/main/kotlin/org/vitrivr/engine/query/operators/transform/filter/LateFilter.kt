package org.vitrivr.engine.query.operators.transform.filter

import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.toList
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.query.basics.ComparisonOperator
import org.vitrivr.engine.core.model.query.basics.ComparisonOperator.*
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.model.retrievable.Retrieved
import org.vitrivr.engine.core.model.types.Value
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.core.operators.general.Transformer
import java.sql.Date
import javax.management.Descriptor

/**
 * Appends [Descriptor] to a [Retrieved] based on the values of a [Schema.Field], if available.
 *
 * @version 1.1.2
 * @author Luca Rossetto
 * @author Ralph Gasser
 */
class LateFilter(
    override val input: Operator<out Retrievable>,
    /* The reader for a given field. */
    private val fieldName: String,
    /* keys to filter on */
    val keys: List<String>,
    /* boolean operator*/
    val comparison: ComparisonOperator = EQ,
    /* value to compare to */
    val value: String,
    /* appends late filter */
    val limit: Int = Int.MAX_VALUE,
    /* on missing key skip */
    val skip: Skip = Skip.valueOf("ignore"),

    override val name: String
) : Transformer {
    private val logger: KLogger = KotlinLogging.logger {}

    override fun toFlow(scope: CoroutineScope): Flow<Retrievable> = flow {
        /* Parse input IDs.*/
        val inputRetrieved = input.toFlow(scope).toList()

        // Multi keys for
        if (keys.size > 1)
            throw IllegalArgumentException("only one key is supported yet")

        var emitted = 0
        /* Emit retrievable with added attribute. */
        inputRetrieved.forEach { retrieved ->

            val descriptors = retrieved.findDescriptor { it.field?.fieldName == fieldName }
            if (descriptors.isEmpty() || descriptors.first().values().containsKey(keys[0]).not()) {
                when (skip) {
                    Skip.ERROR -> throw IllegalArgumentException("no descriptor found for field $fieldName")
                    Skip.WARN -> logger.warn { "no descriptor found for field $fieldName" }
                    Skip.IGNORE -> return@forEach
                    Skip.FORWARD -> emit(retrieved)
                }
            }

            val values = descriptors.first().values().toMap()
            val attribute = keys.map {
                (when (values[it]) {
                    is Value.String -> Pair(it to (values[it] as Value.String), Value.of(value.toString()))
                    is Value.Boolean -> Pair(it to (values[it] as Value.Boolean), Value.of(value.toBoolean()))
                    is Value.Int -> Pair(it to (values[it] as Value.Int), Value.of(value.toInt()))
                    is Value.Long -> Pair(it to (values[it] as Value.Long), Value.of(value.toLong()))
                    is Value.Float -> Pair(it to (values[it] as Value.Float), Value.of(value.toFloat()))
                    is Value.Double -> Pair(it to (values[it] as Value.Double), Value.of(value.toDouble()))
                    is Value.Byte -> Pair(it to (values[it] as Value.Byte), Value.of(value.toByte()))
                    is Value.Short -> Pair(it to (values[it] as Value.Short), Value.of(value.toShort()))
                    is Value.DateTime -> Pair(it to (values[it] as Value.DateTime), Value.of(Date.valueOf(value)))
                    else -> Pair(it to null, null)
                })
            }

            attribute[0].takeIf { it.first.second != null && it.second != null }?.let {
                it.takeIf { ++emitted <= limit && comparison.compare(it.first.second!!, it.second!!) }?.let {
                    emit(retrieved)
                }
            }
        }
    }
}

enum class Skip(val strategy: String) {
    ERROR("error"),
    WARN("warn"),
    IGNORE("ignore"),
    FORWARD("forward");

    companion object {
        /**
         * Resolves a [ComparisonOperator] from the given [String].
         *
         * @param str The [String] which should be one of the [ComparisonOperator]
         * @throws IllegalArgumentException In case the given string is not one of the defined ones.
         */
        infix fun fromString(str: String): Skip {
            return when (str.trim()) {
                ERROR.strategy -> ERROR
                WARN.strategy -> WARN
                IGNORE.strategy -> IGNORE
                FORWARD.strategy -> FORWARD
                else -> throw IllegalArgumentException("Cannot parse '$str")
            }
        }
    }
}

