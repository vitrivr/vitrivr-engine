package org.vitrivr.engine.module.features.feature.external.implementations.identity

import org.vitrivr.engine.core.context.Context
import org.vitrivr.engine.core.model.content.element.ImageContent
import org.vitrivr.engine.core.model.descriptor.struct.LabelDescriptor
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.operators.Operator

/** Stateful forward YOLO + AdaFace + body-ReID identification endpoint. */
class YoloAdaFaceForward : IdentityAnalyser() {
    companion object {
        fun analyse(content: ImageContent, hostname: String, streamId: String): List<LabelDescriptor> = IdentityApi.persons(
            IdentityApi.post(hostname, "/extract/yolo_adaface_forward", IdentityApi.imageParameters(content) + mapOf(
                "stream_id" to streamId, "include_face_embedding" to "false", "include_body_embedding" to "false"
            ))
        )
        fun reset(hostname: String, streamId: String) {
            IdentityApi.post(hostname, "/extract/yolo_adaface_forward/reset", mapOf("stream_id" to streamId))
        }
    }

    override fun newExtractor(field: Schema.Field<ImageContent, LabelDescriptor>, input: Operator<out Retrievable>, context: Context) =
        YoloAdaFaceForwardExtractor(input, this, field, field.parameters[HOST_PARAMETER_NAME] ?: HOST_PARAMETER_DEFAULT)
    override fun newExtractor(name: String, input: Operator<out Retrievable>, context: Context) =
        YoloAdaFaceForwardExtractor(input, this, name, context.getProperty(name, HOST_PARAMETER_NAME) ?: HOST_PARAMETER_DEFAULT)
}
