package org.vitrivr.engine.core.model.metamodel

import org.vitrivr.engine.core.database.Connection
import org.vitrivr.engine.core.database.descriptor.DescriptorInitializer
import org.vitrivr.engine.core.database.descriptor.DescriptorReader
import org.vitrivr.engine.core.database.descriptor.DescriptorWriter
import org.vitrivr.engine.core.model.content.Content
import org.vitrivr.engine.core.model.content.element.ContentElement
import org.vitrivr.engine.core.model.database.descriptor.Descriptor
import org.vitrivr.engine.core.model.database.retrievable.Ingested
import org.vitrivr.engine.core.model.database.retrievable.RetrievableId
import org.vitrivr.engine.core.model.util.DescriptorList
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.core.operators.ingest.*
import org.vitrivr.engine.core.operators.retrieve.Retriever
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
     * @param fieldName The name of the new [Field]. Must be unique.
     */
    fun addField(
        fieldName: String,
        analyser: Analyser<ContentElement<*>, Descriptor>,
        parameters: Map<String, String> = emptyMap()
    ) {
        this.fields.add(Field(fieldName, analyser, parameters))
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
     * Adds a new [Exporter] to this [Schema].
     */
    fun addExporter(
        name: String,
        exporterFactory: ExporterFactory,
        exporterParameters: Map<String, Any>,
        resolverFactory: ResolverFactory,
        resolverParameters: Map<String, Any>
    ) {
        this.exporters.add(Exporter(name, exporterFactory, exporterParameters, resolverFactory, resolverParameters))
    }

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
         * @return [Extractor] instance.
         */
        fun getExtractor(input: Operator<Ingested>): Extractor<C, D> = this.analyser.newExtractor(this, input)

        /**
         * Returns a [Retriever] instance for this [Schema.Field].
         *
         * @param descriptors The [Descriptor](s) that should be used with the [Retriever].
         * @return [Retriever] instance.
         */
        fun getRetriever(descriptors: DescriptorList<D>): Retriever<C, D> =
            this.analyser.newRetriever(this, descriptors)

        /**
         *
         */
        fun getRetriever(descriptor: D): Retriever<C, D> = this.analyser.newRetriever(this, DescriptorList(descriptor))

        /**
         * Returns a [Retriever] instance for this [Schema.Field].
         *
         * @param content The [Content] element(s) that should be used with the [Retriever].
         * @return [Retriever] instance.
         */
        fun getRetriever(content: Collection<C>): Retriever<C, D> = this.analyser.newRetriever(this, content)

        /**
         *
         */
        fun getRetriever(content: C): Retriever<C, D> = this.analyser.newRetriever(this, listOf(content))

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
        val exporterFactory: ExporterFactory,
        val exporterParameters: Map<String, Any> = emptyMap(),
        val resolverFactory: ResolverFactory,
        val resolverParameters: Map<String, Any> = emptyMap()
    ) {
        val schema: Schema
            get() = this@Schema

        val resolver = this.resolverFactory.newResolver(this.resolverParameters)

        public fun getExporter(input: Operator<Ingested>): org.vitrivr.engine.core.operators.ingest.Exporter =
            this.exporterFactory.newOperator(
                input,
                this.exporterParameters,
                this.schema,
                this.resolver
            ) // TODO: Do we even need the schema to manage exporters if we have resolvers?

        fun resolve(id: RetrievableId): Resolvable? = this.resolver.resolve(id)

    }
}