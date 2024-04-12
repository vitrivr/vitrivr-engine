package org.vitrivr.engine.base.features.external.common

import org.vitrivr.engine.core.model.content.element.ContentElement
import org.vitrivr.engine.core.model.descriptor.Descriptor
import org.vitrivr.engine.core.model.metamodel.Analyser
import org.vitrivr.engine.core.model.retrievable.Retrievable


abstract class ExternalFesAnalyser<T : ContentElement<*>, U : Descriptor>: Analyser<T, U> {
    companion object {
        const val HOST_PARAMETER_DEFAULT = "http://localhost:8888/"
        const val HOST_PARAMETER_NAME = "host"
        const val MODEL_PARAMETER_NAME = "model"
    }
    abstract val defaultModel: String


    fun analyse(content: T, parameters: Map<String,String>): U {
        return analyse(listOf(content), parameters).first()
    }

    fun analyse(content: List<T>, parameters: Map<String, String>): List<U> {
        val model = parameters[MODEL_PARAMETER_NAME] ?: defaultModel
        val hostName = parameters[HOST_PARAMETER_NAME] ?: HOST_PARAMETER_DEFAULT
        val apiWrapper = ApiWrapper(hostName, model)
        return analyse(content, apiWrapper, parameters)
    }

    abstract fun analyse(content: List<T>, apiWrapper: ApiWrapper, parameters: Map<String, String>): List<U>



}