package org.vitrivr.engine.base.features.external.common

import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import org.vitrivr.engine.core.model.content.element.ContentElement
import org.vitrivr.engine.core.model.descriptor.Descriptor
import org.vitrivr.engine.core.model.metamodel.Analyser
import org.vitrivr.engine.core.model.retrievable.Retrievable
import java.util.logging.Logger

private val logger: KLogger = KotlinLogging.logger {}

/**
 * Lifts a function that operates on a flat list to a function that operates on a nested list.
 *
 * @param flatFunction The function that operates on a flat list.
 * @return A function that operates on a nested list.
 */
fun <T, R> liftToNestedListFunction(
    flatFunction: (List<T>, ApiWrapper, Map<String, String>) -> List<R>
): (List<List<T>>, ApiWrapper, Map<String, String>) -> List<List<R>> {
    return { nestedList, apiWrapper, parameters ->
        // Flatten the nested list
        val flattenedList = nestedList.flatten()

        // Apply the flat function with additional parameters
        val flatResults = flatFunction(flattenedList, apiWrapper, parameters)

        // Reorganize the results into the nested structure
        var index = 0
        nestedList.map { innerList ->
            innerList.map { _ ->
                flatResults[index++]
            }
        }
    }
}

/**
 * An analyser that uses an external FES API to analyse content.
 *
 * @param T The type of the content to analyse.
 * @param U The type of the descriptor to generate.
 */
abstract class ExternalFesAnalyser<T : ContentElement<*>, U : Descriptor>: Analyser<T, U> {
    companion object {
        const val HOST_PARAMETER_DEFAULT = "http://localhost:8888/"
        const val HOST_PARAMETER_NAME = "host"
        const val MODEL_PARAMETER_NAME = "model"
        const val TIMEOUTSECONDS_PARAMETER_NAME = "timeoutSeconds"
        const val TIMEOUTSECONDS_PARAMETER_DEFAULT = "10"
        const val POLLINGINTERVALMS_PARAMETER_NAME = "pollingIntervalMs"
        const val POLLINGINTERVALMS_PARAMETER_DEFAULT = "100"
        const val BATCHSIZE_PARAMETER_NAME = "batchSize"
        const val BATCHSIZE_PARAMETER_DEFAULT = "1"
    }
    abstract val defaultModel: String


    fun analyse(content: T, parameters: Map<String, String>): U {
        return analyse(listOf(listOf(content)), parameters).first().first()
    }

    fun analyse(content: List<List<T>>, parameters: Map<String, String>): List<List<U>> {
        val model = parameters[MODEL_PARAMETER_NAME] ?: defaultModel
        val hostName = parameters[HOST_PARAMETER_NAME] ?: HOST_PARAMETER_DEFAULT
        val timeoutSeconds = parameters[TIMEOUTSECONDS_PARAMETER_NAME]?.toLongOrNull() ?: TIMEOUTSECONDS_PARAMETER_DEFAULT.toLong()
        val pollingIntervalMs = parameters[POLLINGINTERVALMS_PARAMETER_NAME]?.toLongOrNull() ?: POLLINGINTERVALMS_PARAMETER_DEFAULT.toLong()
        val apiWrapper = ApiWrapper(hostName, model, timeoutSeconds, pollingIntervalMs)

        logger.debug { "Analyzing batch of ${content.size} retrievables with ${this::class.simpleName} analyser." }

        return analyse(content, apiWrapper, parameters)
    }

    /**
     * Analyse the content using the given API wrapper and parameters.
     * This method should be overridden if the extraction should be performed on each content element individually.
     *
     * @param content The content to analyse.
     * @param apiWrapper The API wrapper to use.
     * @param parameters The parameters to use.
     * @return The descriptors generated from the content. The outer list has the same size as the input list, the inner list contains the descriptors for each content element.
     */
    open fun analyseFlattened(content: List<T>, apiWrapper: ApiWrapper, parameters: Map<String, String>): List<List<U>> {
        throw UnsupportedOperationException("Flat analysis not implemented")
    }

    /**
     * Analyse the content using the given API wrapper and parameters.
     * This method should be overridden if the extraction should be performed on the entire content of a retrievable.
     *
     * @param content The content to analyse, grouped by retrievable.
     * @param apiWrapper The API wrapper to use.
     * @param parameters The parameters to use.
     * @return The descriptors generated from the content. The outer list has the same size as the input list, the inner list contains the descriptors for each retrievable.
     */
    open fun analyse(content: List<List<T>>, apiWrapper: ApiWrapper, parameters: Map<String, String>): List<List<U>>{
        logger.debug{ "Analyzing content with ${this::class.simpleName} analyser by flattening batch of ${content.size} retrievables." }
        val output = liftToNestedListFunction(::analyseFlattened)(content, apiWrapper, parameters)
        return output.map { it.flatten() }
    }
}
