package org.vitrivr.engine.index.transform

import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import org.vitrivr.engine.core.context.Context
import org.vitrivr.engine.core.model.descriptor.struct.LabelDescriptor
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.core.operators.OperatorFactory
import org.vitrivr.engine.core.operators.general.Transformer
import org.vitrivr.engine.core.operators.transform.filter.AbstractFilterTransformer
import kotlin.text.toFloat

private val logger: KLogger = KotlinLogging.logger {}

/**
 * A [Transformer] that filters [Retrievable] objects based on their [LabelDescriptor]s.
 *
 * @author Fynn Faber
 * @version 1.2.0
 */
class LabelFilterTransformer : OperatorFactory {
    /**
     * Creates a new [Instance] instance from this [LabelFilterTransformer.Instance].
     *
     * @param name the name of the [LabelFilterTransformer.Instance]
     * @param inputs Map of named input [Operator]s
     * @param context The [Context] to use.
     */
    override fun newOperator(name: String, inputs: Map<String, Operator<out Retrievable>>, context: Context): Operator<out Retrievable> {
        require(inputs.size == 1)  { "The ${this::class.simpleName} only supports one input operator. If you want to combine multiple inputs, use explicit merge strategies." }
        val confidenceThreshold = context[name, "confidenceThreshold"]?.toFloat()
        val label = context[name, "label"]
        val field = context[name, "field"]
        return Instance(name, inputs.values.first(), confidenceThreshold, label, field)
    }

    class Instance(
        name: String,
        input: Operator<out Retrievable>,
        val confidenceThreshold: Float?,
        val label: String?,
        val field: String?
    ) : AbstractFilterTransformer(name, input, { retrievable ->
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
