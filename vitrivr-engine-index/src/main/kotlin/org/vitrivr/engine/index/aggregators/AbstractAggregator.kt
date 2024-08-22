package org.vitrivr.engine.index.aggregators

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.vitrivr.engine.core.context.Context
import org.vitrivr.engine.core.model.content.element.ContentElement
import org.vitrivr.engine.core.model.retrievable.Ingested
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.model.retrievable.attributes.CONTENT_AUTHORS_KEY
import org.vitrivr.engine.core.model.retrievable.attributes.ContentAuthorAttribute
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.core.operators.general.Transformer
import java.util.*

/**
 * An abstract [Transformer] implementation for aggregators; aggregators are used to aggregate the content of [Ingested] objects.
 *
 * @author Ralph Gasser
 * @version 1.1.0
 */
abstract class AbstractAggregator(override val input: Operator<out Retrievable>, protected open val context: Context, protected val name: String, val newContent: Boolean = true) : Transformer {
    /**
     *  Creates a flow for this [AbstractAggregator].
     *
     *  The default [AbstractAggregator] simply copies the incoming [Ingested] and replaces the content with the aggregated content.
     *
     *  @param scope [CoroutineScope] to use for the [Flow].
     */
    override fun toFlow(scope: CoroutineScope): Flow<Retrievable> = this.input.toFlow(scope).map {
        val contentSources = context.getProperty(name, CONTENT_AUTHORS_KEY)?.split(",")?.toSet()
        val contentIds = contentSources?.flatMap { source -> it.filteredAttribute(ContentAuthorAttribute::class.java)?.getContentIds(source) ?: emptySet() }?.toSet()


        if (it.content.isNotEmpty()) {
            val aggregated = this.aggregate(it.content.filter { c -> contentIds?.contains(c.id) ?: true})
            aggregated.forEach { c ->
                if (newContent) {
                    it.addContent(c)
                }
                it.addAttribute(ContentAuthorAttribute(c.id, name))
            }
            it
        } else {
            it
        }
    }

    /**
     * Performs aggregation on a [List] of [ContentElement]s.
     *
     * @param content The [List] of [ContentElement]s.
     */
    abstract fun aggregate(content: List<ContentElement<*>>): List<ContentElement<*>>
}