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
 * An [Analyser] that uses the [ApiWrapper] to analyse content.
 *
 * @param T The type of the [ContentElement] to analyse.
 * @param U The type of the [Descriptor] to generate.
 */
abstract class ExternalFesAnalyser<T : ContentElement<*>, U : Descriptor>: Analyser<T, U> {
    companion object {
        const val HOST_PARAMETER_DEFAULT = "http://localhost:8888/"
        val HOSTS_PARAMETER_DEFAULT: List<String> = listOf("http://localhost:8888/")
        const val HOST_PARAMETER_NAME = "host"
        const val HOSTS_PARAMETER_NAME = "hosts"
        const val MODEL_PARAMETER_NAME = "model"
        const val TIMEOUTSECONDS_PARAMETER_NAME = "timeoutSeconds"
        const val TIMEOUTSECONDS_PARAMETER_DEFAULT = "10"
        const val POLLINGINTERVALMS_PARAMETER_NAME = "pollingIntervalMs"
        const val POLLINGINTERVALMS_PARAMETER_DEFAULT = "100"
        const val BATCHSIZE_PARAMETER_NAME = "batchSize"
        const val BATCHSIZE_PARAMETER_DEFAULT = "1"
    }
    abstract val defaultModel: String

    /**
     * Analyse the [ContentElement] using the given parameters.
     *
     * @param content The [ContentElement] to analyse.
     * @param parameters The parameters to use.
     * @return The (first) [Descriptor] generated from the [ContentElement].
     */
    fun analyse(content: T, parameters: Map<String, String>): U {
        logger.debug { "Analyzing content with ${this::class.simpleName} analyser." }
        return analyse(listOf(listOf(content)), parameters).first().first()
    }

    /**
     * Analyse the [ContentElement] using the given parameters.
     *
     * @param content The [ContentElement] to analyse, grouped by [Retrievable].
     * @param parameters The parameters to use.
     * @return The [Descriptor]s generated from the content, grouped by [Retrievable].
     */
    fun analyse(content: List<List<T>>, parameters: Map<String, Any>): List<List<U>> {
        val model: String = (parameters[MODEL_PARAMETER_NAME] ?: defaultModel as String).toString()
        val hostName:String = (parameters[HOST_PARAMETER_NAME] ?: HOST_PARAMETER_DEFAULT).toString()
        val hostsNames:String = (parameters[HOSTS_PARAMETER_NAME] ?: HOSTS_PARAMETER_DEFAULT).toString()

        val timeoutSeconds = (parameters[TIMEOUTSECONDS_PARAMETER_NAME]?.toString())?.toLongOrNull() ?: TIMEOUTSECONDS_PARAMETER_DEFAULT.toLong()
        val pollingIntervalMs = (parameters[POLLINGINTERVALMS_PARAMETER_NAME]?.toString())?.toLongOrNull() ?: POLLINGINTERVALMS_PARAMETER_DEFAULT.toLong()
        val apiWrapper = ApiWrapper(hostName, hostsNames, model, timeoutSeconds, pollingIntervalMs)

        logger.debug { "Analyzing batch of ${content.size} retrievables with ${this::class.simpleName} analyser." }

        return analyse(content, apiWrapper, parameters)
    }

    /**
     * Analyse the [ContentElement] using the given [ApiWrapper] and parameters.
     * This method should be overridden if the extraction is unimodal i.e. each extraction requires a single content element from a given [Retrievable].
     *
     * @param content The [ContentElement] to analyse.
     * @param apiWrapper The [ApiWrapper] to use for the analysis.
     * @param parameters The parameters to use.
     * @return The [Descriptor]s generated from the content, grouped by [ContentElement]
     */
    open fun analyseFlattened(content: List<T>, apiWrapper: ApiWrapper, parameters: Map<String, String>): List<List<U>> {
        throw UnsupportedOperationException("Flat analysis not implemented")
    }

    /**
     * Analyse the [ContentElement] using the given [ApiWrapper] and parameters.
     * This method should be overridden if the extraction is multimodal i.e. each extraction requires multiple content elements from a given [Retrievable].
     * The default implementation flattens the content and calls the unimodal analysis method.
     *
     * @param content The [ContentElement] to analyse, grouped by [Retrievable].
     * @param apiWrapper The [ApiWrapper] to use for the analysis.
     * @param parameters The parameters to use.
     * @return The [Descriptor]s generated from the content, grouped by [Retrievable].
     */
    open fun analyse(content: List<List<T>>, apiWrapper: ApiWrapper, parameters: Map<String, String>): List<List<U>>{
        logger.debug{ "Analyzing content with ${this::class.simpleName} analyser by flattening batch of ${content.size} retrievables." }
        try {
            val output = liftToNestedListFunction(::analyseFlattened)(content, apiWrapper, parameters)
            return output.map { it.flatten() }
        } catch (e: Exception) {
            "Error while analysing content with ${this::class.simpleName} analyser.".let {
                logger.error { it }
                throw Exception(it, e)
            }
        }
    }
}
