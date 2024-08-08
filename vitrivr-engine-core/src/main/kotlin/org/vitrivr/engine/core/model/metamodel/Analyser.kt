package org.vitrivr.engine.core.model.metamodel

import org.vitrivr.engine.core.context.IndexContext
import org.vitrivr.engine.core.context.QueryContext
import org.vitrivr.engine.core.model.content.Content
import org.vitrivr.engine.core.model.content.element.ContentElement
import org.vitrivr.engine.core.model.descriptor.Descriptor
import org.vitrivr.engine.core.model.query.Query
import org.vitrivr.engine.core.operators.ingest.ExtractorFactory
import org.vitrivr.engine.core.operators.retrieve.Retriever
import kotlin.reflect.KClass

/**
 * An [Analyser] is a formal specification of a type an analysis performed for a [Schema.Field] to derive a [Descriptor].
 *
 * - During indexing, the analysis step involves analysing the media content to derive a [Descriptor]
 * - During retrieval, the analysis step involves the execution of a query using the derived [Descriptor]s.
 *
 * @author Luca Rossetto
 * @author Ralph Gasser
 * @version 1.4.0
 */
interface Analyser<C : ContentElement<*>, D : Descriptor> : ExtractorFactory<C, D> {


    companion object {
        /**
         * Merges the parameters of a [Schema.Field] with the parameters of the [IndexContext].
         *
         * @param field The [Schema.Field] to merge parameters for.
         * @param context The [IndexContext] to merge parameters with.
         * @return Merged parameter map.
         */
        fun merge(field: Schema.Field<*, *>, context: IndexContext): Map<String, String> {
            val fieldParameters = field.parameters
            val contextParameters = context.local[field.fieldName] ?: emptyMap()
            val merged = HashMap<String, String>(contextParameters)
            merged.putAll(fieldParameters)
            return merged
        }

        /**
         * Merges the parameters of a [Schema.Field] with the parameters of the [IndexContext].
         *
         * @param field The [Schema.Field] to merge parameters for.
         * @param context The [QueryContext] to merge parameters with.
         * @return Merged parameter map.
         */
        fun merge(field: Schema.Field<*, *>, context: QueryContext): Map<String, String> {
            val fieldParameters = field.parameters
            val contextParameters = context.local[field.fieldName] ?: emptyMap()
            val merged = HashMap<String, String>(contextParameters)
            merged.putAll(fieldParameters)
            return merged
        }
    }

    /** The [KClass]es of the [ContentElement] accepted by this [Analyser].  */
    val contentClasses: Set<KClass<out ContentElement<*>>>

    /** The [KClass] of the [Descriptor] generated by this [Analyser].  */
    val descriptorClass: KClass<D>

    /**
     * Generates a specimen of the [Descriptor] produced / consumed by this [Analyser] given the provided [Schema.Field]
     * This is a required operation!
     *
     * @param field The [Schema.Field] to create prototype for. Mainly used to support [Analyser]s with descriptors that depend on the [Schema.Field] configuration.ß
     * @return A [Descriptor] specimen of type [D].
     */
    fun prototype(field: Schema.Field<*, *>): D

    /**
     * Generates and returns a new [Retriever] instance for this [Analyser].
     *
     * This is the base-case, every [Analyser] should support this operation unless the [Analyser] is not meant to be used for retrieval at all,
     * in which case the implementation of this method should throw an [UnsupportedOperationException]
     *
     * @param field The [Schema.Field] to create an [Retriever] for.
     * @param query The [Query] to use with the [Retriever].
     * @param context The [QueryContext] to use with the [Retriever].
     *
     * @return A new [Retriever] instance for this [Analyser]
     * @throws [UnsupportedOperationException], if this [Analyser] does not support the creation of an [Retriever] instance.
     */
    fun newRetrieverForQuery(field: Schema.Field<C, D>, query: Query, context: QueryContext): Retriever<C, D>

    /**
     * Generates and returns a new [Retriever] instance for this [Analyser].
     *
     * Some [Analyser]s may not come with their own [Retriever] or may not support generating a [Retriever] from [Content].
     * In both chases, the implementation of this method should throw an [UnsupportedOperationException].
     *
     * @param field The [Schema.Field] to create an [Retriever] for.
     * @param content An array of [Content] elements to use with the [Retriever]
     * @param context The [QueryContext] to use with the [Retriever]
     *
     * @return A new [Retriever] instance for this [Analyser]
     * @throws [UnsupportedOperationException], if this [Analyser] does not support the creation of an [Retriever] instance from [Content].
     */
    fun newRetrieverForContent(field: Schema.Field<C, D>, content: Collection<C>, context: QueryContext): Retriever<C, D> {
        throw UnsupportedOperationException("This Analyser does not support the creation of a retriever from a collection of descriptors.")
    }

    /**
     * Generates and returns a new [Retriever] instance for this [Analyser] from the provided [Collection] of [Descriptor]s.
     *
     * Some [Analyser]s may not come with their own [Retriever] or may not support generating a [Retriever] from a [Descriptor].
     * In both chases, the implementation of this method should throw an [UnsupportedOperationException].
     *
     * @param field The [Schema.Field] to create an [Retriever] for.
     * @param descriptors An array of [Descriptor] elements to use with the [Retriever]
     * @param context The [QueryContext] to use with the [Retriever]
     *
     * @return A new [Retriever] instance for this [Analyser]
     * @throws [UnsupportedOperationException], if this [Analyser] does not support the creation of an [Retriever] instance from [Descriptor]s.
     */
    fun newRetrieverForDescriptors(field: Schema.Field<C, D>, descriptors: Collection<D>, context: QueryContext): Retriever<C, D> {
        throw UnsupportedOperationException("This Analyser does not support the creation of a retriever from a collection of descriptors.")
    }
}