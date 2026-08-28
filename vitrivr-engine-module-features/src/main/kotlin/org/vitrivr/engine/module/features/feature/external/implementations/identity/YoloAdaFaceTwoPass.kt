package org.vitrivr.engine.module.features.feature.external.implementations.identity

import org.vitrivr.engine.core.context.Context
import org.vitrivr.engine.core.model.content.element.ImageContent
import org.vitrivr.engine.core.model.descriptor.struct.LabelDescriptor
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.operators.Operator

/** Retroactive two-pass YOLO + AdaFace + body-ReID identification endpoint. */
class YoloAdaFaceTwoPass : IdentityAnalyser() {
    companion object {
        fun submit(content: ImageContent, hostname: String, streamId: String, frameIndex: Int) {
            IdentityApi.post(hostname, "/extract/yolo_adaface_two_pass", IdentityApi.imageParameters(content) + mapOf(
                "stream_id" to streamId, "frame_index" to frameIndex.toString(),
                "include_face_embedding" to "false", "include_body_embedding" to "false"
            ))
        }
        fun finalize(hostname: String, streamId: String): Map<Int, List<LabelDescriptor>> = IdentityApi.finalizedFrames(
            IdentityApi.post(hostname, "/extract/yolo_adaface_two_pass/finalize", mapOf("stream_id" to streamId))
        )
        fun reset(hostname: String, streamId: String) {
            IdentityApi.post(hostname, "/extract/yolo_adaface_two_pass/reset", mapOf("stream_id" to streamId))
        }
    }

    override fun newExtractor(field: Schema.Field<ImageContent, LabelDescriptor>, input: Operator<out Retrievable>, context: Context) =
        YoloAdaFaceTwoPassExtractor(input, this, field, field.parameters[HOST_PARAMETER_NAME] ?: HOST_PARAMETER_DEFAULT)
    override fun newExtractor(name: String, input: Operator<out Retrievable>, context: Context) =
        YoloAdaFaceTwoPassExtractor(input, this, name, context.getProperty(name, HOST_PARAMETER_NAME) ?: HOST_PARAMETER_DEFAULT)
}
