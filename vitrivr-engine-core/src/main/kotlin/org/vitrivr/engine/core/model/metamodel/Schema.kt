package org.vitrivr.engine.core.model.metamodel

import org.vitrivr.engine.core.context.IndexContext
import org.vitrivr.engine.core.context.QueryContext
import org.vitrivr.engine.core.database.Connection
import org.vitrivr.engine.core.database.descriptor.DescriptorInitializer
import org.vitrivr.engine.core.database.descriptor.DescriptorReader
import org.vitrivr.engine.core.database.descriptor.DescriptorWriter
import org.vitrivr.engine.core.model.content.Content
import org.vitrivr.engine.core.model.content.element.ContentElement
import org.vitrivr.engine.core.model.descriptor.Descriptor
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.core.operators.ingest.ExporterFactory
import org.vitrivr.engine.core.operators.ingest.Extractor
import org.vitrivr.engine.core.operators.retrieve.Retriever
import org.vitrivr.engine.core.resolver.Resolver
import org.vitrivr.engine.core.resolver.ResolverFactory
import java.io.Closeable
import java.util.*

typealias FieldName = String

/**
 * A [Schema] that defines a particular vitrivr instance's meta data model.
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
class Schema(val name: String = "vitrivr", val connection: Connection) : Closeable {

    /** The [List] of [Field]s contained in this [Schema]. */
    private val fields: MutableList<Schema.Field<ContentElement<*>, Descriptor>> = mutableListOf()

    /** The [List] of [Exporter]s contained in this [Schema]. */
    private val exporters: MutableList<Schema.Exporter> = mutableListOf()

    /**
     * Adds a new [Field] to this [Schema].
     *
     * @param name The name of the new [Field]. Must be unique.
     */
    fun addField(name: String, analyser: Analyser<ContentElement<*>, Descriptor>, parameters: Map<String, String> = emptyMap()) {
        this.fields.add(Field(name, analyser, parameters))
    }

    /**
     * Adds a new [Exporter] to this [Schema].
     *
     * @param name The name of the [Exporter]. Must be unique.
     * @param factory The [ExporterFactory] used to generated instance.
     * @param parameters The parameters used to configure the [Exporter].
     * @param resolver The [Resolver] instance.
     */
    fun addExporter(name: String, factory: ExporterFactory, parameters: Map<String, Any>, resolver: Resolver) {
        this.exporters.add(Exporter(name, factory, parameters, resolver))
    }

    /**
     * Lists the [Schema.Field] contained in this [Schema].
     *
     * @return Unmodifiable list of [Schema.Field].
     */
    fun fields(): List<Schema.Field<ContentElement<*>, Descriptor>> = Collections.unmodifiableList(this.fields)

    /**
     * Returns the field at the provided [index].
     *
     * @param index The index to return the [Schema.Field] for.
     * @return [Schema.Field]
     */
    operator fun get(index: Int) = this.fields[index]

    /**
     * Returns the field for the provided name.
     *
     * @param name The name of the [Schema.Field] to return.
     * @return [Schema.Field] or null, if no such [Schema.Field] exists.
     */
    operator fun get(name: String) = this.fields.firstOrNull { it.fieldName == name }

    /**
     * Returns the exporter for the provided name.
     *
     * @param name The name of the [Schema.Exporter] to return.
     * @return [Schema.Exporter] or null, if no such [Schema.Exporter] exists.
     */
    fun getExporter(name: String) = this.exporters.firstOrNull { it.name == name }


    /**
     * Closes this [Schema] and the associated database [Connection].
     */
    override fun close() = this.connection.close()

    /**
     * A [Field] that is part of a [Schema].
     *
     * A [Field] always has a unique name and is backed by an existing [Analyser].
     */
    inner class Field<C : ContentElement<*>, D : Descriptor>(
        val fieldName: FieldName,
        val analyser: Analyser<C, D>,
        val parameters: Map<String, String> = emptyMap()
    ) {
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
         * @param context The [IndexContext] to use with the [Extractor].
         * @return [Extractor] instance.
         */
        fun getExtractor(input: Operator<Retrievable>, context: IndexContext): Extractor<C, D> = this.analyser.newExtractor(this, input, context, true)

        /**
         * Returns a [Retriever] instance for this [Schema.Field].
         *
         * @param descriptor The [Descriptor](s) that should be used with the [Retriever].
         * @param context The [QueryContext] to use with the [Retriever].
         * @return [Retriever] instance.
         */
        fun getRetrieverForDescriptor(descriptor: D, context: QueryContext): Retriever<C, D> = this.analyser.newRetrieverForDescriptors(this, listOf(descriptor), context)

        /**
         * Returns a [Retriever] instance for this [Schema.Field].
         *
         * @param content The [Content](s) that should be used with the [Retriever].
         * @param context The [QueryContext] to use with the [Retriever].

         * @return [Retriever] instance.
         */
        fun getRetrieverForContent(content: C, context: QueryContext): Retriever<C, D> = this.analyser.newRetrieverForContent(this, listOf(content), context)

        /**
         * Returns a [Retriever] instance for this [Schema.Field].
         *
         * @param descriptors The [Descriptor](s) that should be used with the [Retriever].
         * @param context The [QueryContext] to use with the [Retriever].
         * @return [Retriever] instance.
         */
        fun getRetrieverForDescriptors(descriptors: Collection<D>, context: QueryContext): Retriever<C, D> = this.analyser.newRetrieverForDescriptors(this, descriptors, context)

        /**
         * Returns a [Retriever] instance for this [Schema.Field].
         *
         * @param content The [Content] element(s) that should be used with the [Retriever].
         * @return [Retriever] instance.
         */
        fun getRetrieverForContent(content: Collection<C>, queryContext: QueryContext): Retriever<C, D> = this.analyser.newRetrieverForContent(this, content, queryContext)

        /**
         * Returns the [DescriptorInitializer] for this [Schema.Field].
         *
         * @return [DescriptorInitializer]
         */
        fun getInitializer(): DescriptorInitializer<D> = this.connection.getDescriptorInitializer(this)

        /**
         * Convenience method to generate and return a [DescriptorReader] for this [Field].
         *
         * @return [DescriptorReader]
         */
        fun getReader(): DescriptorReader<D> = this@Schema.connection.getDescriptorReader(this as Field<*, D>)

        /**
         * Convenience method to generate and return a [DescriptorWriter] for this [Field].
         *
         * @return [DescriptorWriter]
         */
        fun getWriter(): DescriptorWriter<D> = this@Schema.connection.getDescriptorWriter(this as Field<*, D>)
    }

    /**
     * An [Exporter] that is part of a [Schema].
     *
     * An [Exporter] always has a unique name and is backed by an existing [ExporterFactory] and an existing [ResolverFactory].
     */
    inner class Exporter(
        val name: String,
        private val factory: ExporterFactory,
        private val parameters: Map<String, Any> = emptyMap(),
        val resolver: Resolver
    ) {
        val schema: Schema
            get() = this@Schema

        /**
         * Convenience method to generate and return a [org.vitrivr.engine.core.operators.ingest.Exporter ] for this [Exporter].
         *
         * @param input The [Operator] to use as input.
         * @param context The [IndexContext] to use.
         * @return [DescriptorReader]
         */
        fun getExporter(input: Operator<Retrievable>, context: IndexContext): org.vitrivr.engine.core.operators.ingest.Exporter = this.factory.newOperator(input, context, this.parameters)
    }
}