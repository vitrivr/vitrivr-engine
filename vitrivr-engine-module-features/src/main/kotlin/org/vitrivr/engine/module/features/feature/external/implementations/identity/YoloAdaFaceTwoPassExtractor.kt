package org.vitrivr.engine.module.features.feature.external.implementations.identity

import org.vitrivr.engine.core.features.AbstractExtractor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.vitrivr.engine.core.model.content.element.ImageContent
import org.vitrivr.engine.core.model.descriptor.struct.LabelDescriptor
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.model.retrievable.attributes.DescriptorAuthorAttribute
import org.vitrivr.engine.core.model.retrievable.attributes.SourceAttribute
import org.vitrivr.engine.core.source.MediaType
import org.vitrivr.engine.core.source.SourceId
import org.vitrivr.engine.core.operators.Operator

class YoloAdaFaceTwoPassExtractor : AbstractExtractor<ImageContent, LabelDescriptor> {
    private val host: String
    constructor(input: Operator<out Retrievable>, analyser: YoloAdaFaceTwoPass, field: Schema.Field<ImageContent, LabelDescriptor>, host: String) : super(input, analyser, field) { this.host = host }
    constructor(input: Operator<out Retrievable>, analyser: YoloAdaFaceTwoPass, name: String, host: String) : super(input, analyser, name) { this.host = host }

    private data class BufferedRetrievable(
        val retrievable: Retrievable,
        val frameIndices: MutableList<Int> = mutableListOf()
    )

    private data class VideoState(
        var nextFrameIndex: Int = 0,
        val retrievables: MutableList<BufferedRetrievable> = mutableListOf()
    )

    override fun toFlow(scope: CoroutineScope): Flow<Retrievable> = flow {
        val states = mutableMapOf<SourceId, VideoState>()

        suspend fun finish(sourceId: SourceId) {
            val state = states.remove(sourceId) ?: return
            val streamId = sourceId.toString()
            try {
                val finalized = YoloAdaFaceTwoPass.finalize(host, streamId)
                for (buffered in state.retrievables) {
                    val descriptors = buffered.frameIndices.flatMap { finalized[it].orEmpty() }
                        .map { it.copy(retrievableId = buffered.retrievable.id, field = field) }
                    if (descriptors.isEmpty()) {
                        emit(buffered.retrievable)
                        continue
                    }

                    writer?.addAll(descriptors)
                    val author = DescriptorAuthorAttribute()
                    descriptors.forEach { author.add(it, name) }
                    emit(buffered.retrievable.copy(
                        descriptors = buffered.retrievable.descriptors + descriptors,
                        attributes = buffered.retrievable.attributes + author
                    ))
                }
            } catch (e: Throwable) {
                logger.error(e) { "Failed to finalize two-pass identification for video $sourceId." }
                state.retrievables.forEach { emit(it.retrievable) }
            } finally {
                runCatching { YoloAdaFaceTwoPass.reset(host, streamId) }
                    .onFailure { logger.error(it) { "Failed to reset two-pass identification for video $sourceId." } }
            }
        }

        input.toFlow(scope).collect { retrievable ->
            val source = retrievable.filteredAttribute(SourceAttribute::class.java)?.source
            if (retrievable.type == VIDEO_END_TYPE) {
                source?.sourceId?.let { finish(it) }
                emit(retrievable)
                return@collect
            }

            val images = retrievable.content.filterIsInstance<ImageContent>()
            if (source?.type != MediaType.VIDEO || images.isEmpty()) {
                emit(retrievable)
                return@collect
            }

            val state = states.getOrPut(source.sourceId) { VideoState() }
            val buffered = BufferedRetrievable(retrievable)
            try {
                images.forEach { image ->
                    val frameIndex = state.nextFrameIndex++
                    YoloAdaFaceTwoPass.submit(image, host, source.sourceId.toString(), frameIndex)
                    buffered.frameIndices.add(frameIndex)
                }
                state.retrievables.add(buffered)
            } catch (e: Throwable) {
                logger.error(e) { "Failed to submit frame for two-pass identification of video ${source.sourceId}." }
                state.retrievables.add(buffered)
            }
        }

        /* Avoid losing buffered data if an upstream implementation omits the terminal signal. */
        states.keys.toList().forEach { finish(it) }
    }

    override fun extract(retrievable: Retrievable): List<LabelDescriptor> = emptyList()

    private companion object {
        const val VIDEO_END_TYPE = "SOURCE:VIDEO"
    }
}
