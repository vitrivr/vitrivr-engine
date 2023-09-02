package org.vitrivr.engine.core.model.metamodel

import org.vitrivr.engine.core.database.Connection
import org.vitrivr.engine.core.database.descriptor.DescriptorReader
import org.vitrivr.engine.core.database.descriptor.DescriptorWriter
import org.vitrivr.engine.core.model.content.Content
import org.vitrivr.engine.core.model.database.descriptor.Descriptor
import org.vitrivr.engine.core.model.database.retrievable.IngestedRetrievable
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.core.operators.ingest.Extractor
import org.vitrivr.engine.core.operators.ingest.Segmenter
import org.vitrivr.engine.core.operators.retrieve.Retriever
import java.io.Closeable

typealias FieldName = String

/**
 * A [Schema] that defines a particular vitrivr instance's meta data model.
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
class Schema(val name: String = "vitrivr", val connection: Connection): Closeable {

    /** The [List] of [Field]s contained in this [Schema]. */
    private val fields: MutableList<Schema.Field<Content,Descriptor>> = mutableListOf()

    /**
     * Adds a new [Field] to this [Schema].
     *
     * @param fieldName The name of the new [Field]. Must be unique.
     */
    fun addField(fieldName: String, analyser: Analyser<Content,Descriptor>, parameters: Map<String,String> = emptyMap()) {
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
            val initializer = this.connection.getDescriptorInitializer(field as Field<*,Descriptor>)
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
    inner class Field<C: Content, D: Descriptor>(val fieldName: FieldName, val analyser: Analyser<C,D>, val parameters: Map<String,String> = emptyMap()) {

        /** Pointer to the [Schema] this [Field] belongs to.*/
        val schema: Schema
            get() = this@Schema

        /** Pointer to the [Connection] backing this [Field].*/
        val connection: Connection
            get() = this@Schema.connection

        /**
         * Returns an [Extractor] instances for this [Schema.Field].
         *
         * @param input The input [Operator] for the [Extractor].
         * @return [Extractor] instance.
         */
        fun getExtractor(input: Operator<IngestedRetrievable>): Extractor<C,D> = this.analyser.newExtractor(this, input)

        /**
         * Returns a [Retriever] instance for this [Schema.Field].
         *
         * @param descriptors The [Descriptor](s) that should be used with the [Retriever].
         * @return [Retriever] instance.
         */
        fun getRetriever(vararg descriptors: D): Retriever<C,D> = this.analyser.newRetriever(this, *descriptors)

        /**
         * Returns a [Retriever] instance for this [Schema.Field].
         *
         * @param content The [Content] element(s) that should be used with the [Retriever].
         * @return [Retriever] instance.
         */
        fun getRetriever(vararg content: C): Retriever<C,D> = this.analyser.newRetriever(this, *content)

        /**
         * Convenience method to generate and return a [DescriptorReader] for this [Field].
         *
         * @return [DescriptorReader]
         */
        fun getReader(): DescriptorReader<D> = this@Schema.connection.getDescriptorReader(this as Field<*,D>)

        /**
         * Convenience method to generate and return a [DescriptorWriter] for this [Field].
         *
         * @return [DescriptorWriter]
         */
        fun getWriter(): DescriptorWriter<D> = this@Schema.connection.getDescriptorWriter(this as Field<*,D>)
    }
}