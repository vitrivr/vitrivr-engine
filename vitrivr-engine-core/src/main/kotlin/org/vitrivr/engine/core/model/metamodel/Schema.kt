package org.vitrivr.engine.core.model.metamodel

import org.vitrivr.engine.core.database.Connection
import org.vitrivr.engine.core.database.descriptor.DescriptorReader
import org.vitrivr.engine.core.database.descriptor.DescriptorWriter
import org.vitrivr.engine.core.model.database.descriptor.Descriptor
import org.vitrivr.engine.core.model.database.retrievable.IngestedRetrievable
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.core.operators.ingest.Extractor
import java.io.Closeable


/**
 * A [Schema] that defines a particular vitrivr instance's meta data model.
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
class Schema(val name: String = "vitrivr", val connection: Connection): Closeable {

    /** The [List] of [Field]s contained in this [Schema]. */
    private val fields: MutableList<Field<*>> = mutableListOf()

    /**
     * Adds a new [Field] to this [Schema].
     *
     * @param name The name of the new [Field]. Must be unique.
     */
    fun addField(fieldName: String, analyser: Analyser<*>, parameters: Map<String,String> = emptyMap()) {
        this.fields.add(Field(fieldName, analyser, parameters))
    }

    fun getField(index: Int) = this.fields[index]

    fun getField(fieldName: String) = this.fields.firstOrNull { it.fieldName == fieldName }

    /**
     * Initializes this [Schema] using the provided database [Connection].
     */
    fun initialize() {
        this.connection.getRetrievableInitializer().initialize()
        for (field in fields) {
            val initializer = this.connection.getDescriptorInitializer(field)
            initializer.initialize()
        }
    }

    /**
     * Closes this [Schema] and the associated database [Connection].
     */
    override fun close() = this.connection.close()

    /**
     * A [Field] that is part of a [Schema].
     *
     * A [Field] always has a unique name and is backed by an existing [Analyser].
     */
    inner class Field<T: Descriptor>(val fieldName: String, val analyser: Analyser<T>, val parameters: Map<String,String> = emptyMap()) {

        /** Pointer to the [Schema] this [Field] belongs to.*/
        val schema: Schema
            get() = this@Schema

        /** Pointer to the [Connection] backing this [Field].*/
        val connection: Connection
            get() = this@Schema.connection

        /**
         * Convenience method to generate and return a new [Extractor] for this [Field].
         *
         * @param input [Operator] of [IngestedRetrievable] that acts as input to the [Extractor].
         * @param persisting Flag indicating, whether [Extractor] should persist information.
         * @return Created [Extractor]
         */
        fun getExtractor(input: Operator<IngestedRetrievable>, persisting: Boolean = true): Extractor<T> = this.analyser.newExtractor(this, input, persisting)

        /**
         * Convenience method to generate and return a [DescriptorReader] for this [Field].
         *
         * @return [DescriptorReader]
         */
        fun getReader(): DescriptorReader<T> = this@Schema.connection.getDescriptorReader(this)

        /**
         * Convenience method to generate and return a [DescriptorWriter] for this [Field].
         *
         * @return [DescriptorWriter]
         */
        fun getWriter(): DescriptorWriter<T> = this@Schema.connection.getDescriptorWriter(this)
    }
}