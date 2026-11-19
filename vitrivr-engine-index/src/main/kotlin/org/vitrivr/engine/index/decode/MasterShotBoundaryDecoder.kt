package org.vitrivr.engine.index.decode

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import org.bytedeco.javacv.FFmpegFrameGrabber
import org.bytedeco.javacv.FrameGrabber
import org.bytedeco.javacv.Java2DFrameConverter
import org.vitrivr.engine.core.context.Context
import org.vitrivr.engine.core.model.content.element.ContentElement
import org.vitrivr.engine.core.model.relationship.Relationship
import org.vitrivr.engine.core.model.retrievable.Ingested
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.model.retrievable.attributes.SourceAttribute
import org.vitrivr.engine.core.model.retrievable.attributes.time.ShotAudioRangeAttribute
import org.vitrivr.engine.core.model.retrievable.attributes.time.TimeRangeAttribute
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.core.operators.OperatorFactory
import org.vitrivr.engine.core.operators.ingest.Decoder
import org.vitrivr.engine.core.source.MediaType
import org.vitrivr.engine.core.source.file.FileSource
import org.vitrivr.engine.index.util.boundary.ShotBoundaryProviderFactory
import org.vitrivr.engine.core.util.extension.loadServiceForName
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.ShortBuffer
import java.util.UUID
import kotlin.math.ceil
import kotlin.math.roundToInt

/** Decodes one representative frame and one audio segment per inclusive master-shot boundary. */
class MasterShotBoundaryDecoder : OperatorFactory {
    override fun newOperator(
        name: String, inputs: Map<String, Operator<out Retrievable>>, context: Context
    ): Decoder {
        val input = inputs.values.single()
        val providerName = context[name, "sbProvider"] ?: "FileSystemShotBoundaryProvider"
        val provider = requireNotNull(loadServiceForName<ShotBoundaryProviderFactory>(providerName)) {
            "Unknown shot boundary provider: $providerName."
        }.newShotBoundaryProvider(name, context)
        val sampleSize = context[name, "videoSampleSize"]?.toInt() ?: 512
        val decodeShotAudio = context[name, "decodeShotAudio"]?.toBooleanStrict() ?: true
        require(sampleSize > 0) { "videoSampleSize must be positive." }

        return object : Decoder {
            override val name = name
            override val input = input

            override fun toFlow(scope: CoroutineScope) = flow {
                input.toFlow(scope).collect { sourceItem ->
                    val source = sourceItem.filteredAttribute(SourceAttribute::class.java)?.source
                    if (source?.type != MediaType.VIDEO) {
                        emit(sourceItem)
                        return@collect
                    }
                    require(source is FileSource) { "MasterShotBoundaryDecoder requires a local video file." }
                    val shots = provider.decode(source.path.fileName.toString().substringBeforeLast('.'))
                    require(shots.isNotEmpty()) { "No master-shot boundaries for ${source.name}." }
                    require(shots.all { it.start >= 0 && it.end >= it.start && !it.startAbs.isNegative && it.endAbs >= it.startAbs } &&
                        shots.zipWithNext().all { (a, b) -> a.end < b.start && a.endAbs < b.startAbs }) {
                        "Master-shot boundaries must be ordered and non-overlapping for ${source.name}."
                    }
                    FFmpegFrameGrabber(source.path.toFile()).use { video ->
                        video.start()
                        val fps = video.videoFrameRate
                        require(fps.isFinite() && fps > 0) { "Invalid video frame rate for ${source.name}." }
                        emit(sourceItem)
                        FFmpegFrameGrabber(source.path.toFile()).use { audio ->
                            val hasAudio = decodeShotAudio && video.audioChannels > 0
                            if (hasAudio) {
                                audio.audioChannels = 1
                                audio.sampleRate = AUDIO_SAMPLE_RATE
                                audio.sampleMode = FrameGrabber.SampleMode.SHORT
                                audio.start()
                            }
                            Java2DFrameConverter().use { converter ->
                                var frameIndex = -1
                                val audioCursor = if (hasAudio) ShotAudioCursor(audio) else null
                                for ((index, shot) in shots.withIndex()) {
                                    val target = shot.start + (shot.end - shot.start) / 2
                                    while (frameIndex < target - 1) {
                                        // Decode intervening frames for codec references without converting their pixels.
                                        val skipped = video.grabFrame(
                                            /* doAudio = */ false,
                                            /* doVideo = */ true,
                                            /* doProcessing = */ false,
                                            /* keyFrames = */ false,
                                            /* doData = */ false
                                        )
                                        checkNotNull(skipped) { "Missing frame $target in ${source.name}." }
                                        frameIndex++
                                    }
                                    val frame = checkNotNull(video.grabImage()) { "Missing frame $target in ${source.name}." }
                                    frameIndex++
                                    val image = resizedCopy(converter.convert(frame), sampleSize)
                                    val content = mutableListOf<ContentElement<*>>(context.contentFactory.newImageContent(image))
                                    val start = shot.startAbs.toNanos()
                                    val end = shot.endAbs.toNanos()
                                    val audioEnd = end + (1_000_000_000.0 / fps).toLong()
                                    audioCursor?.read(start, audioEnd)?.let { samples ->
                                        content.add(context.contentFactory.newAudioContent(1, AUDIO_SAMPLE_RATE, samples))
                                    }
                                    val nextStart = shots.getOrNull(index + 1)?.startAbs?.toNanos()
                                    val audioEndExclusive = if (nextStart == null) audioEnd else minOf(audioEnd, nextStart)
                                    val id = UUID.randomUUID()
                                    emit(Ingested(
                                        id, "SEGMENT", content,
                                        attributes = setOf(
                                            SourceAttribute(source), TimeRangeAttribute(start, end),
                                            ShotAudioRangeAttribute(start, audioEndExclusive)
                                        ),
                                        relationships = setOf(Relationship.ById(id, "partOf", sourceItem.id, false)),
                                        transient = false
                                    ))
                                }
                            }
                        }
                    }
                }
            }.flowOn(Dispatchers.IO)
        }
    }

    private fun resizedCopy(image: BufferedImage, maxSide: Int): BufferedImage {
        val scale = (maxSide.toDouble() / maxOf(image.width, image.height)).coerceAtMost(1.0)
        val copy = BufferedImage(
            (image.width * scale).roundToInt().coerceAtLeast(1),
            (image.height * scale).roundToInt().coerceAtLeast(1),
            BufferedImage.TYPE_INT_RGB
        )
        val graphics = copy.createGraphics()
        try {
            graphics.drawImage(image, 0, 0, copy.width, copy.height, null)
        } finally {
            graphics.dispose()
        }
        return copy
    }

    /** Keeps the first audio frame that crosses a boundary for the next shot. */
    private class ShotAudioCursor(private val audio: FFmpegFrameGrabber) {
        private var frame = audio.grabSamples()

        fun read(startNs: Long, endNs: Long): ShortBuffer? {
            val bytes = ByteArrayOutputStream()
            while (true) {
                val current = frame ?: break
                val start = current.timestamp * 1000
                if (start >= endNs) break
                val samples = (current.samples.single() as ShortBuffer).duplicate()
                val count = samples.remaining()
                val from = sampleOffset(startNs - start, count)
                val until = sampleOffset(endNs - start, count)
                for (i in from until until) {
                    val value = samples[samples.position() + i].toInt()
                    bytes.write(value and 0xff)
                    bytes.write((value shr 8) and 0xff)
                }
                if (until < count) break
                frame = audio.grabSamples()
            }
            if (bytes.size() == 0) return null
            return ByteBuffer.wrap(bytes.toByteArray()).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer()
        }

        private fun sampleOffset(offsetNs: Long, count: Int): Int =
            ceil(offsetNs * (AUDIO_SAMPLE_RATE / 1_000_000_000.0)).toInt().coerceIn(0, count)
    }

    private companion object {
        const val AUDIO_SAMPLE_RATE = 16000
    }
}
