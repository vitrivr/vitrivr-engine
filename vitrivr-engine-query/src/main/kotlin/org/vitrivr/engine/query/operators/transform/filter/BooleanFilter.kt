package org.vitrivr.engine.query.operators.transform.filter

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.toList
import org.vitrivr.engine.core.database.descriptor.DescriptorReader
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.query.basics.ComparisonOperator
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.model.retrievable.Retrieved
import org.vitrivr.engine.core.model.retrievable.attributes.PropertyAttribute
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
class BooleanFilter(
    override val input: Operator<out Retrievable>,
    /* The reader for a given field. */
    private val reader: DescriptorReader<*>,
    /* keys to filter on */
    val keys: List<String>,
    /* boolean operator*/
    val comparison: ComparisonOperator = ComparisonOperator.EQ,
    /* value to compare to */
    val value: String,

    override val name: String
) : Transformer {
    override fun toFlow(scope: CoroutineScope): Flow<Retrievable> = flow {
        /* Parse input IDs.*/
        val inputRetrieved = input.toFlow(scope).toList()

        /* Fetch entries for the provided IDs. */
        val ids = inputRetrieved.map { it.id }.toSet()
        val descriptors = if (ids.isEmpty()) {
            emptyMap()
        } else {
            this@BooleanFilter.reader.getAllForRetrievable(ids).associateBy { it.retrievableId!! }
        }

        if (keys.size > 1)
            throw IllegalArgumentException("only one key is supported yet")

        /* Emit retrievable with added attribute. */
        inputRetrieved.forEach { retrieved ->
            val descriptor = descriptors[retrieved.id]
            if (descriptor != null) {
                //retrieved.addDescriptor(descriptor)
                /* Somewhat experimental. Goal: Attach information in a meaningful manner, such that it can be serialised */
                val values = descriptor.values().toMap()
                val attribute = keys.map {
                    (when (values[it]) {
                        is Value.String -> Pair(
                            (values[it] as Value.String),
                            Value.of(value.toString()) as Value.String
                        )

                        is Value.Boolean -> Pair(
                            (values[it] as Value.Boolean),
                            Value.of(value.toBoolean()) as Value.Boolean
                        )

                        is Value.Int -> Pair((values[it] as Value.Int), Value.of(value.toInt()) as Value.Int)
                        is Value.Long -> Pair((values[it] as Value.Long), Value.of(value.toLong()) as Value.Long)
                        is Value.Float -> Pair((values[it] as Value.Float), Value.of(value.toFloat()) as Value.Float)
                        is Value.Double -> Pair(
                            (values[it] as Value.Double),
                            Value.of(value.toDouble()) as Value.Double
                        )

                        is Value.Byte -> Pair((values[it] as Value.Byte), Value.of(value.toByte()) as Value.Byte)
                        is Value.Short -> Pair((values[it] as Value.Short), Value.of(value.toShort()) as Value.Short)
                        is Value.DateTime -> Pair(
                            (values[it] as Value.DateTime),
                            Value.of(Date.valueOf(value)) as Value.DateTime
                        )

                        else -> Pair(null, null)
                    })
                }

                if (attribute[0].first != null && attribute[0].second != null && comparison.compare(
                        attribute[0].first!!,
                        attribute[0].second!!
                    )
                ) {
                    emit(retrieved)
                }
            }
        }
    }
}
