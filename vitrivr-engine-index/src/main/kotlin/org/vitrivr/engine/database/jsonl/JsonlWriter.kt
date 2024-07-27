package org.vitrivr.engine.database.jsonl

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.vitrivr.engine.core.database.descriptor.DescriptorWriter
import org.vitrivr.engine.core.model.descriptor.AttributeName
import org.vitrivr.engine.core.model.descriptor.Descriptor
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.types.Value
import org.vitrivr.engine.database.jsonl.JsonlConnection.Companion.DESCRIPTOR_ID_COLUMN_NAME
import org.vitrivr.engine.database.jsonl.JsonlConnection.Companion.RETRIEVABLE_ID_COLUMN_NAME
import org.vitrivr.engine.database.jsonl.model.AttributeContainer
import org.vitrivr.engine.database.jsonl.model.AttributeContainerList
import java.io.FileWriter


class JsonlWriter<D : Descriptor>(override val field: Schema.Field<*, D>, override val connection: JsonlConnection) :
    DescriptorWriter<D>, AutoCloseable {

    private val writer = FileWriter(connection.getFile(field), true)

    override fun add(item: D): Boolean {

        val valueMap = mutableMapOf<AttributeName, Value<*>?>(
            DESCRIPTOR_ID_COLUMN_NAME to Value.UUIDValue(item.id),
            RETRIEVABLE_ID_COLUMN_NAME to Value.UUIDValue(
                item.retrievableId
                    ?: throw IllegalArgumentException("A struct descriptor must be associated with a retrievable ID.")
            )
        )

        valueMap.putAll(item.values())

        val list = AttributeContainerList(
            item.layout().map { attribute ->
                AttributeContainer(
                    attribute,
                    valueMap[attribute.name]
                )
            }
        )


        writer.write(Json.encodeToString(list))
        writer.write("\n")
        writer.flush()

        return true

    }

    override fun addAll(items: Iterable<D>): Boolean {
        items.forEach { add(it) }
        return true
    }

    override fun update(item: D): Boolean {
        LOGGER.warn { "JsonlWriter.update is not supported" }
        return false
    }

    override fun delete(item: D): Boolean {
        LOGGER.warn { "JsonlWriter.delete is not supported" }
        return false
    }

    override fun deleteAll(items: Iterable<D>): Boolean {
        LOGGER.warn { "JsonlWriter.deleteAll is not supported" }
        return false
    }

    override fun close() {
        writer.close()
    }
}