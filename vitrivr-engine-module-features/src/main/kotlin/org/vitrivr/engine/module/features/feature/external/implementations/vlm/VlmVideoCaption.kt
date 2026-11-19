package org.vitrivr.engine.module.features.feature.external.implementations.vlm

import org.vitrivr.engine.core.context.Context
import org.vitrivr.engine.core.model.content.element.ContentElement
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.source.file.MimeType
import java.io.IOException
import kotlin.reflect.KClass

/**
 * Generates a caption from the complete MP4 clip associated with a shot ID.
 * Its schema field supports independent full-text retrieval over those captions.
 *
 * @author Rahel Arnold
 */
class VlmVideoCaption : VlmCaption(
    "Describe what is visible in this video shot in two or three concise, factual sentences. " +
        "Mention important people, objects, actions, and visible changes in the order they occur. " +
        "Describe only what the video shows and avoid inferring intentions or events outside the shot."
) {
    override val contentClasses: Set<KClass<out ContentElement<*>>> = emptySet()

    override fun descriptionSource(
        client: OpenAiVlmClient, parameters: Map<String, String>, context: Context
    ): (Retrievable) -> VlmDescription? {
        val resolverName = requireNotNull(parameters["videoResolver"]) { "VlmVideoCaption requires videoResolver." }
        val resolver = requireNotNull(context.resolver[resolverName]) { "Unknown video resolver: $resolverName." }
        val maxBytes = parameters["maxVideoBytes"]?.toInt() ?: 16 * 1024 * 1024
        require(maxBytes in 1 until Int.MAX_VALUE) { "maxVideoBytes must be positive and less than ${Int.MAX_VALUE}." }
        val shotType = parameters["shotType"] ?: "SEGMENT"

        return describe@{ shot ->
            if (shot.type != shotType) return@describe null
            val clip = resolver.resolve(shot.id, MimeType.MP4.fileExtension)
            if (clip == null || !clip.exists()) throw IOException("Missing MP4 clip for shot ${shot.id}.")
            val bytes = clip.openInputStream().use { it.readNBytes(maxBytes + 1) }
            if (bytes.isEmpty()) throw IOException("Empty MP4 clip for shot ${shot.id}.")
            if (bytes.size > maxBytes) throw IOException("MP4 clip for shot ${shot.id} exceeds maxVideoBytes ($maxBytes).")
            client.describeVideo(bytes)
        }
    }
}
