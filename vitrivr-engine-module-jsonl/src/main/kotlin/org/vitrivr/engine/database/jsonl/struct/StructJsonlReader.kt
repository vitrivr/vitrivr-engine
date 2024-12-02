package org.vitrivr.engine.database.jsonl.struct

import org.vitrivr.engine.core.model.descriptor.AttributeName
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
import kotlin.reflect.full.primaryConstructor

class StructJsonlReader(
    field: Schema.Field<*, StructDescriptor<*>>,
    connection: JsonlConnection
) : AbstractJsonlReader<StructDescriptor<*>>(field, connection) {

    override fun toDescriptor(list: AttributeContainerList): StructDescriptor<*> {

        val map = list.list.associateBy { it.attribute.name }
        val constructor = this.field.analyser.descriptorClass.primaryConstructor
            ?: throw IllegalStateException("Provided type ${this.field.analyser.descriptorClass} does not have a primary constructor.")
        val valueMap = mutableMapOf<AttributeName, Value<*>>()

        val retrievableId = (map[RETRIEVABLE_ID_COLUMN_NAME]?.value!!.toValue() as Value.UUIDValue).value
        val descriptorId = (map[DESCRIPTOR_ID_COLUMN_NAME]?.value!!.toValue() as Value.UUIDValue).value
        val parameters: MutableList<Any?> = mutableListOf(
            descriptorId,
            retrievableId,
            valueMap,
            this.field
        )

        prototype.layout().forEach { attribute ->
            val value = map[attribute.name]!!.value?.toValue()!!
            valueMap[attribute.name] = value
        }

        return constructor.call(*parameters.toTypedArray())
    }

    override fun query(query: Query): Sequence<StructDescriptor<*>> = when (val predicate = query.predicate) {
        is SimpleFulltextPredicate -> this.queryFulltext(predicate)
        is Comparison<*> -> this.queryBoolean(predicate)
        else -> throw UnsupportedOperationException("The provided query type ${query::class.simpleName} is not supported by this reader.")
    }


    private fun queryFulltext(fulltextQuery: SimpleFulltextPredicate): Sequence<StructDescriptor<*>> {

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
    private fun queryBoolean(query: Comparison<*>): Sequence<StructDescriptor<*>> = when (query) {
        is Comparison.Equals<*> -> getAll().filter { descriptor ->
            descriptor.values()[query.attributeName!!] == query.value
        }

        is Comparison.NotEquals<*> -> getAll().filter { descriptor ->
            descriptor.values()[query.attributeName!!] != query.value
        }

        is Comparison.Greater<*> -> getAll().filter { descriptor ->
            (descriptor.values()[query.attributeName!!] as ScalarValue<Any>) > (query.value as ScalarValue<Any>)
        }

        is Comparison.GreaterEquals<*> -> getAll().filter { descriptor ->
            (descriptor.values()[query.attributeName!!] as ScalarValue<Any>) >= (query.value as ScalarValue<Any>)
        }

        is Comparison.Less<*> -> getAll().filter { descriptor ->
            (descriptor.values()[query.attributeName!!] as ScalarValue<Any>) <= (query.value as ScalarValue<Any>)
        }

        is Comparison.LessEquals<*> -> getAll().filter { descriptor ->
            (descriptor.values()[query.attributeName!!] as ScalarValue<Any>) <= (query.value as ScalarValue<Any>)
        }

        is Comparison.In<*> -> getAll().filter { descriptor ->
            query.values.contains(descriptor.values()[query.attributeName!!])
        }

        is Comparison.Like<*> -> {
            val regex = when (query.value) {
                is Value.String -> query.value.value.likeToRegex()
                is Value.Text -> query.value.value.likeToRegex()
                else -> throw IllegalArgumentException("LIKE comparison is only supported for String and Text values.")
            }
            getAll().filter { descriptor ->
                val value = descriptor.values()[query.attributeName!!]
                when (value) {
                    is Value.String -> regex.matches(value.value)
                    is Value.Text -> regex.matches(value.value)
                    else -> false
                }
            }
        }
    }


}