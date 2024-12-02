package org.vitrivr.engine.database.jsonl.scalar

import org.vitrivr.engine.core.model.descriptor.scalar.*
import org.vitrivr.engine.core.model.descriptor.struct.StructDescriptor
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.query.Query
import org.vitrivr.engine.core.model.query.bool.Comparison
import org.vitrivr.engine.core.model.query.fulltext.SimpleFulltextPredicate
import org.vitrivr.engine.core.model.types.Value
import org.vitrivr.engine.core.model.types.Value.ScalarValue
import org.vitrivr.engine.database.jsonl.AbstractJsonlReader
import org.vitrivr.engine.database.jsonl.JsonlConnection
import org.vitrivr.engine.database.jsonl.JsonlConnection.Companion.DESCRIPTOR_ID_COLUMN_NAME
import org.vitrivr.engine.database.jsonl.JsonlConnection.Companion.RETRIEVABLE_ID_COLUMN_NAME
import org.vitrivr.engine.database.jsonl.likeToRegex
import org.vitrivr.engine.database.jsonl.model.AttributeContainerList

class ScalarJsonlReader(
    field: Schema.Field<*, ScalarDescriptor<*, *>>,
    connection: JsonlConnection
) : AbstractJsonlReader<ScalarDescriptor<*, *>>(field, connection) {

    override fun toDescriptor(list: AttributeContainerList): ScalarDescriptor<*, *> {

        val map = list.list.associateBy { it.attribute.name }
        val retrievableId = (map[RETRIEVABLE_ID_COLUMN_NAME]?.value!!.toValue() as Value.UUIDValue).value
        val descriptorId = (map[DESCRIPTOR_ID_COLUMN_NAME]?.value!!.toValue() as Value.UUIDValue).value
        val value = map["value"]?.value!!.toValue()

        return when (prototype) {
            is BooleanDescriptor -> BooleanDescriptor(retrievableId, descriptorId, value as Value.Boolean)
            is DoubleDescriptor -> DoubleDescriptor(retrievableId, descriptorId, value as Value.Double)
            is FloatDescriptor -> FloatDescriptor(retrievableId, descriptorId, value as Value.Float)
            is IntDescriptor -> IntDescriptor(retrievableId, descriptorId, value as Value.Int)
            is LongDescriptor -> LongDescriptor(retrievableId, descriptorId, value as Value.Long)
            is StringDescriptor -> StringDescriptor(retrievableId, descriptorId, value as Value.String)
            else -> {
                error("Unsupported type $prototype")
            }
        }

    }

    override fun query(query: Query): Sequence<ScalarDescriptor<*, *>> = when (val predicate = query.predicate) {
        is SimpleFulltextPredicate -> this.queryFulltext(predicate)
        is Comparison<*> -> this.queryBoolean(predicate)
        else -> throw UnsupportedOperationException("The provided query type ${query::class.simpleName} is not supported by this reader.")
    }


    private fun queryFulltext(fulltextQuery: SimpleFulltextPredicate): Sequence<ScalarDescriptor<*, *>> {

        val queryString = fulltextQuery.value.value
        val attributeName = fulltextQuery.attributeName ?: return emptySequence()

        return getAll().filter { descriptor ->
            (descriptor.values()[attributeName]!! as Value.String).value.contains(queryString)
        }

    }

    /**
     * Executes a [Comparison] query and returns a [Sequence] of [StructDescriptor]s.
     *
     * @param query The [Comparison] query to execute.
     * @return [Sequence] of [StructDescriptor]s.
     */
    @Suppress("UNCHECKED_CAST")
    private fun queryBoolean(query: Comparison<*>): Sequence<ScalarDescriptor<*, *>> = when (query) {
        is Comparison.Equals<*> -> getAll().filter { descriptor ->
            descriptor.value == query.value
        }

        is Comparison.NotEquals<*> -> getAll().filter { descriptor ->
            descriptor.value != query.value
        }

        is Comparison.Greater<*> -> getAll().filter { descriptor ->
            descriptor.value as ScalarValue<Any> > (query.value as ScalarValue<Any>)
        }

        is Comparison.GreaterEquals<*> -> getAll().filter { descriptor ->
            descriptor.value as ScalarValue<Any> >= (query.value as ScalarValue<Any>)
        }

        is Comparison.Less<*> -> getAll().filter { descriptor ->
            descriptor.value as ScalarValue<Any> <= (query.value as ScalarValue<Any>)
        }

        is Comparison.LessEquals<*> -> getAll().filter { descriptor ->
            descriptor.value as ScalarValue<Any> <= (query.value as ScalarValue<Any>)
        }

        is Comparison.In<*> -> getAll().filter { descriptor ->
            query.values.contains(descriptor.value)
        }

        is Comparison.Like<*> -> {
            val regex = when (query.value) {
                is Value.String -> query.value.value.likeToRegex()
                is Value.Text -> query.value.value.likeToRegex()
                else -> throw IllegalArgumentException("LIKE comparison is only supported for String and Text values.")
            }
            getAll().filter { descriptor ->
                val value = descriptor.value
                when (value) {
                    is Value.String -> regex.matches(value.value)
                    is Value.Text -> regex.matches(value.value)
                    else -> false
                }
            }
        }
    }
}
