package org.vitrivr.engine.query.operators.transform.filter

import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.toList
import org.vitrivr.engine.core.database.descriptor.DescriptorReader
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.query.basics.ComparisonOperator
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
class FieldLookupLateFilter(
    override val input: Operator<out Retrievable>,
    /* The reader for a given field. */
    private val reader: DescriptorReader<*>,
    /* keys to filter on */
    val keys: List<String>,
    /* boolean operator*/
    val comparison: ComparisonOperator = ComparisonOperator.EQ,
    /* value to compare to */
    val value: String,
    /* append field*/
    val append: Boolean,
    /* appends late filter */
    val limit: Int = Int.MAX_VALUE,
    override val name: String
) : Transformer {
    private val logger: KLogger = KotlinLogging.logger {}

    override fun toFlow(scope: CoroutineScope): Flow<Retrievable> = flow {
        /* Parse input IDs.*/
        val inputRetrieved = input.toFlow(scope).toList()

        /* Fetch entries for the provided IDs. */
        val ids = inputRetrieved.map { it.id }.toSet()
        val descriptors = if (ids.isEmpty()) {
            emptyMap()
        } else {
            this@FieldLookupLateFilter.reader.getAllForRetrievable(ids).associateBy { it.retrievableId!! }
        }

        // Multi keys for
        if (keys.size > 1)
            throw IllegalArgumentException("only one key is supported yet")

        var emitted = 0
        /* Emit retrievable with added attribute. */
        inputRetrieved.forEach { retrieved ->
            val descriptor = descriptors[retrieved.id]
            if (descriptor != null) {
                /* Somewhat experimental. Goal: Attach information in a meaningful manner, such that it can be serialised */
                val values = descriptor.values().toMap()
                val attribute = keys.map {
                    (when (values[it]) {
                        is Value.String -> Pair(it to (values[it] as Value.String), Value.of(value.toString()))
                        is Value.Text -> Pair(it to (values[it] as Value.Text), Value.of(value.toString()))
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
                        if (append) {
                            emit(retrieved.copy(descriptors = retrieved.descriptors + descriptor))
                        } else {
                            emit(retrieved)
                        }
                    }
                }
            }
        }
    }
}
