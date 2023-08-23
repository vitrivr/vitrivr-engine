package org.vitrivr.engine.core.model.metamodel

import org.vitrivr.engine.core.database.Connection
import org.vitrivr.engine.core.database.Reader
import org.vitrivr.engine.core.database.descriptor.DescriptorReader
import org.vitrivr.engine.core.database.descriptor.DescriptorWriter
import org.vitrivr.engine.core.model.database.descriptor.Descriptor
import java.io.Closeable
import java.util.LinkedList


/**
 * A [Schema] that defines a particular vitrivr instance's meta data model.
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
class Schema(val name: String = "vitrivr", val connection: Connection, ): Closeable {

    /** */
    val fields: List<Field<*>> = LinkedList()

    /**
     * Closes this [Schema] and the associated [Connection].
     */
    override fun close() = this.connection.close()

    /**
     *
     */
    fun addField(fieldName: String, analyser: Analyser<*>, parameters: Map<String,String> = emptyMap()) {
        (this.fields as LinkedList<Field<*>>).add(Field(fieldName, analyser, parameters))
    }

    /**
     *
     */
    inner class Field<T: Descriptor>(val fieldName: String, val analyser: Analyser<T>, val parameters: Map<String,String> = emptyMap()) {
        /**
         *
         */
        fun getReader(): DescriptorReader<T> = this@Schema.connection.getDescriptorReader(this)

        /**
         *
         */
        fun getWriter(): DescriptorWriter<T> = this@Schema.connection.getDescriptorWriter(this)

        /**
         *
         */
        fun schema(): Schema = this@Schema

        /**
         *
         */
        fun connection(): Connection = this@Schema.connection
    }
}