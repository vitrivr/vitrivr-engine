package org.vitrivr.engine.module.features.feature.external.implementations.identity

import org.vitrivr.engine.core.context.Context
import org.vitrivr.engine.core.model.content.element.ImageContent
import org.vitrivr.engine.core.model.descriptor.struct.LabelDescriptor
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.operators.Operator

/** Stateless AdaFace face identification endpoint. */
class AdaFace : IdentityAnalyser() {
    companion object {
        fun analyse(content: ImageContent, hostname: String): List<LabelDescriptor> = IdentityApi.faces(
            IdentityApi.post(hostname, "/extract/adaface", IdentityApi.imageParameters(content) + mapOf(
                "identify" to "true", "include_embedding" to "false"
            ))
        )
    }

    override fun newExtractor(field: Schema.Field<ImageContent, LabelDescriptor>, input: Operator<out Retrievable>, context: Context) =
        AdaFaceExtractor(input, this, field, field.parameters[HOST_PARAMETER_NAME] ?: HOST_PARAMETER_DEFAULT)

    override fun newExtractor(name: String, input: Operator<out Retrievable>, context: Context) =
        AdaFaceExtractor(input, this, name, context.getProperty(name, HOST_PARAMETER_NAME) ?: HOST_PARAMETER_DEFAULT)
}
