package org.vitrivr.engine.index.decode

import com.github.kokorin.jaffree.StreamType
import com.github.kokorin.jaffree.ffmpeg.*
import com.github.kokorin.jaffree.ffprobe.FFprobe
import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel.Factory.RENDEZVOUS
import kotlinx.coroutines.channels.ProducerScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.channelFlow
import org.bytedeco.javacv.FrameGrabber
import org.vitrivr.engine.core.context.IndexContext
import org.vitrivr.engine.core.model.relationship.Relationship
import org.vitrivr.engine.core.model.retrievable.Ingested
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.model.retrievable.attributes.ContentAuthorAttribute
import org.vitrivr.engine.core.model.retrievable.attributes.SourceAttribute
import org.vitrivr.engine.core.model.retrievable.attributes.time.TimeRangeAttribute
import org.vitrivr.engine.core.operators.ingest.Decoder
import org.vitrivr.engine.core.operators.ingest.DecoderFactory
import org.vitrivr.engine.core.operators.ingest.Enumerator
import org.vitrivr.engine.core.source.MediaType
import org.vitrivr.engine.core.source.file.FileSource
import java.awt.image.BufferedImage
import java.nio.file.Path
import java.util.*
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit
import org.vitrivr.engine.core.source.Metadata

class FFmpegVideoDecoder : DecoderFactory {

    override fun newDecoder(name: String, input: Enumerator, context: IndexContext): Decoder {
        val maxWidth = context[name, "maxWidth"]?.toIntOrNull() ?: 3840
        val maxHeight = context[name, "maxHeight"]?.toIntOrNull() ?: 2160
        val framerate = context[name, "framerate"]?.toIntOrNull()
        val timeWindowMs = context[name, "timeWindowMs"]?.toLongOrNull() ?: 500L
        val ffmpegPath = context[name, "ffmpegPath"]?.let { Path.of(it) }

        return Instance(input, context, timeWindowMs, maxWidth, maxHeight, framerate, name, ffmpegPath)
    }

    private class Instance(
        override val input: Enumerator,
        private val context: IndexContext,
        private val timeWindowMs: Long = 500L,
        private val maxWidth: Int,
        private val maxHeight: Int,
        private val framerate: Int?,
        private val name: String,
        private val ffmpegPath: Path?
    ) : Decoder {

        /** [KLogger] instance. */
        private val logger: KLogger = KotlinLogging.logger {}

        private val ffprobe: FFprobe
            get() = if (ffmpegPath != null) FFprobe.atPath(this.ffmpegPath) else FFprobe.atPath()

        private val ffmpeg: FFmpeg
            get() = if (ffmpegPath != null) FFmpeg.atPath(this.ffmpegPath) else FFmpeg.atPath()

        override fun toFlow(scope: CoroutineScope): Flow<Retrievable> = channelFlow {
            this@Instance.input.toFlow(scope).collect { sourceRetrievable ->
                /* Extract source. */
                val source = sourceRetrievable.filteredAttribute(SourceAttribute::class.java)?.source ?: return@collect
                if (source.type != MediaType.VIDEO) {
                    logger.debug { "In flow: Skipping source ${source.name} (${source.sourceId}) because it is not of type VIDEO." }
                    return@collect
                }

                val probeResult = ffprobe.setShowStreams(true).also {
                    if (source is FileSource) {
                        it.setInput(source.path)
                    } else {
                        it.setInput(source.newInputStream())
                    }
                }.execute()

                val videoStreamInfo = probeResult.streams.find { it.codecType == StreamType.VIDEO }

                if (videoStreamInfo != null) {
                    source.metadata[Metadata.METADATA_KEY_VIDEO_FPS] = videoStreamInfo.avgFrameRate.toDouble()
                    source.metadata[Metadata.METADATA_KEY_AV_DURATION] = (videoStreamInfo.duration * 1000f).toLong()
                    source.metadata[Metadata.METADATA_KEY_IMAGE_WIDTH] = videoStreamInfo.width
                    source.metadata[Metadata.METADATA_KEY_IMAGE_HEIGHT] = videoStreamInfo.height
                }

                val audioStreamInfo = probeResult.streams.find { it.codecType == StreamType.AUDIO }

                if (audioStreamInfo != null) {
                    source.metadata[Metadata.METADATA_KEY_AUDIO_CHANNELS] = audioStreamInfo.channels
                    source.metadata[Metadata.METADATA_KEY_AUDIO_SAMPLERATE] = audioStreamInfo.sampleRate
                    source.metadata[Metadata.METADATA_KEY_AUDIO_SAMPLESIZE] = audioStreamInfo.sampleFmt
                }

                var windowEnd = TimeUnit.MILLISECONDS.toMicros(this@Instance.timeWindowMs)

                val imageTransferBuffer = LinkedBlockingQueue<Pair<BufferedImage, Long>>(10)

                val ffmpegInstance = ffmpeg.addInput(
                    if (source is FileSource) {
                        UrlInput.fromPath(source.path)
                    } else {
                        PipeInput.pumpFrom(source.newInputStream())
                    }
                ).addOutput(
                    FrameOutput.withConsumerAlpha(
                        object : FrameConsumer {

                            val streamMap = mutableMapOf<Int, Stream>()

                            override fun consumeStreams(streams: MutableList<Stream>) {
                                streams.forEach { stream -> streamMap[stream.id] = stream }
                            }

                            override fun consume(frame: Frame) {

                                val stream = streamMap[frame.streamId] ?: return

                                when (stream.type) {
                                    Stream.Type.VIDEO -> {
                                        imageTransferBuffer.put(frame.image!! to (1000000 * frame.pts) / stream.timebase)
                                    }

                                    Stream.Type.AUDIO -> {
                                        //TODO
                                    }

                                    null -> {
                                        /* ignore */
                                    }
                                }
                            }

                        }
                    )
                ).setFilter(
                    StreamType.VIDEO,
                    "scale=w='min($maxWidth,iw)':h='min($maxHeight,ih)':force_original_aspect_ratio=decrease${if (framerate != null && framerate > 0) ",fps=$framerate" else ""}'"
                )

                //TODO audio settings

                val future = ffmpegInstance.executeAsync()

                val localImageBuffer = LinkedList<Pair<BufferedImage, Long>>()

                while (!(future.isDone || future.isCancelled) || imageTransferBuffer.isNotEmpty()) {

                    val next = imageTransferBuffer.poll(1, TimeUnit.SECONDS) ?: continue
                    localImageBuffer.add(next)

                    if (localImageBuffer.last().second >= windowEnd) {
                        emit(localImageBuffer, windowEnd, sourceRetrievable, this@channelFlow)
                        windowEnd += TimeUnit.MILLISECONDS.toMicros(this@Instance.timeWindowMs)
                    }

                }

                while (localImageBuffer.isNotEmpty()) {
                    emit(localImageBuffer, windowEnd, sourceRetrievable, this@channelFlow)
                    windowEnd += TimeUnit.MILLISECONDS.toMicros(this@Instance.timeWindowMs)
                }

                send(sourceRetrievable)

            }
        }.buffer(capacity = RENDEZVOUS, onBufferOverflow = BufferOverflow.SUSPEND)


        /**
         * Emits a single [Retrievable] to the downstream [channel].
         *
         * @param imageBuffer A [LinkedList] containing [BufferedImage] elements to emit (frames).
         * @param grabber The [FrameGrabber] instance.
         * @param timestampEnd The end timestamp.
         * @param source The source [Retrievable] the emitted [Retrievable] is part of.
         */
        private suspend fun emit(
            imageBuffer: MutableList<Pair<BufferedImage, Long>>,
            timestampEnd: Long,
            source: Retrievable,
            channel: ProducerScope<Retrievable>
        ) {

            val emitImage = mutableListOf<BufferedImage>()

            /* Drain buffer. */
            imageBuffer.removeIf {
                if (it.second <= timestampEnd) {
                    emitImage.add(it.first)
                    true
                } else {
                    false
                }
            }


            /* Prepare ingested with relationship to source. */
            val ingested = Ingested(UUID.randomUUID(), "SEGMENT", false)
            source.filteredAttribute(SourceAttribute::class.java)?.let { ingested.addAttribute(it) }
            ingested.addRelationship(Relationship.ByRef(ingested, "partOf", source, false))
            ingested.addAttribute(
                TimeRangeAttribute(
                    timestampEnd - TimeUnit.MILLISECONDS.toMicros(this@Instance.timeWindowMs),
                    timestampEnd,
                    TimeUnit.MICROSECONDS
                )
            )

            /* Prepare and append image content element. */
            for (image in emitImage) {
                val imageContent = this.context.contentFactory.newImageContent(image)
                ingested.addContent(imageContent)
                ingested.addAttribute(ContentAuthorAttribute(imageContent.id, name))
            }

            logger.debug { "Emitting ingested ${ingested.id} with ${emitImage.size} images: ${ingested.id}" }

            /* Emit ingested. */
            channel.send(ingested)
        }

    }
}