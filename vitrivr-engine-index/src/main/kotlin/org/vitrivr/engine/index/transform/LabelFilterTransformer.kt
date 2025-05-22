package org.vitrivr.engine.index.transform

import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import org.vitrivr.engine.core.context.Context
import org.vitrivr.engine.core.context.IndexContext
import org.vitrivr.engine.core.model.descriptor.struct.LabelDescriptor
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.core.operators.general.Transformer
import org.vitrivr.engine.core.operators.general.TransformerFactory
import org.vitrivr.engine.core.operators.transform.filter.AbstractFilterTransformer

private val logger: KLogger = KotlinLogging.logger {}

/**
 * A [Transformer] that filters [Retrievable] objects based on their [LabelDescriptor]s.
 *
 * @author Fynn Faber
 * @version 1.0.0
 */
class LabelFilterTransformer : TransformerFactory {

    override fun newTransformer(
        name: String,
        input: Operator<out Retrievable>,
        parameters: Map<String, String>,
        context: Context
    ): Transformer {
        val confidenceThreshold = parameters["confidenceThreshold"]?.toFloat()
        val label = parameters["label"]
        val field = parameters["field"]
        return Instance(input, name, confidenceThreshold, label, field)
    }

    class Instance(
        input: Operator<out Retrievable>,
        override val name: String,
        val confidenceThreshold: Float?,
        val label: String?,
        val field: String?
    ) : AbstractFilterTransformer(input, { retrievable ->
        val isFiltered = retrievable.descriptors.any { descriptor ->
            if (descriptor is LabelDescriptor) {
                val labelMatches = label?.let { descriptor.label.value == it } ?: true
                val confidenceMatches = confidenceThreshold?.let { descriptor.confidence.value >= it } ?: true
                val fieldMatches = field?.let { descriptor.field?.fieldName == it } ?: true

                labelMatches && confidenceMatches && fieldMatches
            } else {
                false
            }
        }

        if (isFiltered) {
            logger.debug { "Retrievable with ID ${retrievable.id} has been filtered in by ${label} filter." }
        } else {
            logger.debug { "Retrievable with ID ${retrievable.id} has been filtered out by ${label} filter." }
        }

        isFiltered
    })
}
