package org.vitrivr.engine.database.jsonl.struct

import org.vitrivr.engine.core.model.descriptor.AttributeName
import org.vitrivr.engine.core.model.descriptor.struct.StructDescriptor
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.query.Query
import org.vitrivr.engine.core.model.query.bool.SimpleBooleanQuery
import org.vitrivr.engine.core.model.query.fulltext.SimpleFulltextQuery
import org.vitrivr.engine.core.model.types.Value
import org.vitrivr.engine.database.jsonl.AbstractJsonlReader
import org.vitrivr.engine.database.jsonl.JsonlConnection
import org.vitrivr.engine.database.jsonl.JsonlConnection.Companion.DESCRIPTOR_ID_COLUMN_NAME
import org.vitrivr.engine.database.jsonl.JsonlConnection.Companion.RETRIEVABLE_ID_COLUMN_NAME
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

        val retrievableId = (map[DESCRIPTOR_ID_COLUMN_NAME]?.value!!.toValue() as Value.UUIDValue).value
        val descriptorId = (map[RETRIEVABLE_ID_COLUMN_NAME]?.value!!.toValue() as Value.UUIDValue).value
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

    override fun query(query: Query): Sequence<StructDescriptor<*>> = when (query) {
        is SimpleFulltextQuery -> this.queryFulltext(query)
        is SimpleBooleanQuery<*> -> this.queryBoolean(query)
        else -> throw UnsupportedOperationException("The provided query type ${query::class.simpleName} is not supported by this reader.")
    }


    private fun queryFulltext(fulltextQuery: SimpleFulltextQuery): Sequence<StructDescriptor<*>> {

        val queryString = fulltextQuery.value.value
        val attributeName = fulltextQuery.attributeName ?: return emptySequence()

        return getAll().filter { descriptor ->
            (descriptor.values()[attributeName]!! as Value.String).value.contains(queryString)
        }

    }

    private fun queryBoolean(query: SimpleBooleanQuery<*>): Sequence<StructDescriptor<*>> = getAll().filter { descriptor ->
        query.comparison.compare(descriptor.values()[query.attributeName!!]!!, query.value)
    }


}