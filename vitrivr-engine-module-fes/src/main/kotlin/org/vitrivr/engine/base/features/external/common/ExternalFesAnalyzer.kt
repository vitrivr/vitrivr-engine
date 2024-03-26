package org.vitrivr.engine.base.features.external.common

import org.vitrivr.engine.core.model.content.element.ContentElement
import org.vitrivr.engine.core.model.descriptor.Descriptor
import org.vitrivr.engine.core.model.metamodel.Analyser

abstract class ExternalFesAnalyzer<T : ContentElement<*>, U : Descriptor>: Analyser<T, U> {
    companion object {
        const val HOST_PARAMETER_DEFAULT = "http://localhost:8888/"
        const val HOST_PARAMETER_NAME = "host"
    }

    abstract fun analyse(content: T, model: String, hostName: String): U

}