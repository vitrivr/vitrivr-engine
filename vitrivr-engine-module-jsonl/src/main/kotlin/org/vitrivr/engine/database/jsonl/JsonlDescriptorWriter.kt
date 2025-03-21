package org.vitrivr.engine.database.jsonl

import kotlinx.serialization.json.Json
import org.vitrivr.engine.core.database.descriptor.DescriptorWriter
import org.vitrivr.engine.core.model.descriptor.Attribute
import org.vitrivr.engine.core.model.descriptor.AttributeName
import org.vitrivr.engine.core.model.descriptor.Descriptor
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.types.Type
import org.vitrivr.engine.core.model.types.Value
import org.vitrivr.engine.database.jsonl.JsonlConnection.Companion.DESCRIPTOR_ID_COLUMN_NAME
import org.vitrivr.engine.database.jsonl.JsonlConnection.Companion.RETRIEVABLE_ID_COLUMN_NAME
import org.vitrivr.engine.database.jsonl.model.AttributeContainer
import org.vitrivr.engine.database.jsonl.model.AttributeContainerList
import java.io.OutputStreamWriter
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import kotlin.io.path.writer

/**
 * A [DescriptorWriter] for the JSONL format. This class is responsible for writing [Descriptor]s to a JSONL file.
 *
 * @author Luca Rossetto
 * @author Ralph Gasser
 * @version 1.1.0
 */
class JsonlDescriptorWriter<D : Descriptor<*>>(override val field: Schema.Field<*, D>, override val connection: JsonlConnection) : DescriptorWriter<D> {

    /** The [Path] to the JSONL file. */
    private val path: Path = this.connection.resolve(field)

    /**
     * Adds (writes) a single [Descriptor] using this [JsonlDescriptorWriter].
     *
     * @param item The [Descriptor] to write.
     * @return True on success, false otherwise.
     */
    override fun add(item: D): Boolean {
        this.path.writer(Charsets.UTF_8, StandardOpenOption.APPEND).use { writer ->
            writer.writeRecord(item)
        }
        return true
    }

    /**
     * Adds (writes) a batch of [Descriptor] of type [D] using this [PgDescriptorWriter].
     *
     * @param items A [Iterable] of [Descriptor]s to write.
     * @return True on success, false otherwise.
     */
    override fun addAll(items: Iterable<D>): Boolean {
        this.path.writer(Charsets.UTF_8, StandardOpenOption.APPEND).use { writer ->
            items.forEach {
                writer.writeRecord(it)
            }
        }
        return true
    }

    /**
     * Writes a single record of type [D] to the [OutputStreamWriter].
     *
     * @param item The item [D] to write.
     */
    protected fun OutputStreamWriter.writeRecord(item: D) {
        val valueMap = mutableMapOf<AttributeName, Value<*>?>(
            DESCRIPTOR_ID_COLUMN_NAME to Value.UUIDValue(item.id),
            RETRIEVABLE_ID_COLUMN_NAME to Value.UUIDValue(
                item.retrievableId
                    ?: throw IllegalArgumentException("A struct descriptor must be associated with a retrievable ID.")
            )
        )

        valueMap.putAll(item.values())
        val attributes = mutableListOf(
            Attribute(DESCRIPTOR_ID_COLUMN_NAME, Type.UUID, false),
            Attribute(RETRIEVABLE_ID_COLUMN_NAME, Type.UUID, false)
        )
        attributes.addAll(item.layout())

        val list = AttributeContainerList(
            attributes.map { attribute ->
                AttributeContainer(
                    attribute,
                    valueMap[attribute.name]
                )
            }
        )

        this.write(Json.encodeToString(list))
        this.write("\n")
        this.flush()
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
}