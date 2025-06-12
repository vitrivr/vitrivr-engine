package org.vitrivr.engine.index.decode

import com.github.kokorin.jaffree.StreamType
import com.github.kokorin.jaffree.ffmpeg.*
import com.github.kokorin.jaffree.ffprobe.FFprobe
import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel.Factory.RENDEZVOUS
import kotlinx.coroutines.channels.ProducerScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.runBlocking
import org.vitrivr.engine.core.context.IndexContext
import org.vitrivr.engine.core.model.content.element.AudioContent
import org.vitrivr.engine.core.model.content.element.ContentElement
import org.vitrivr.engine.core.model.content.element.ImageContent
import org.vitrivr.engine.core.model.relationship.Relationship
import org.vitrivr.engine.core.model.retrievable.Ingested
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.model.retrievable.attributes.RetrievableAttribute
import org.vitrivr.engine.core.model.retrievable.attributes.SourceAttribute
import org.vitrivr.engine.core.model.retrievable.attributes.time.TimeRangeAttribute
import org.vitrivr.engine.core.operators.ingest.Decoder
import org.vitrivr.engine.core.operators.ingest.DecoderFactory
import org.vitrivr.engine.core.operators.ingest.Enumerator
import org.vitrivr.engine.core.source.MediaType
import org.vitrivr.engine.core.source.Metadata
import org.vitrivr.engine.core.source.Source
import org.vitrivr.engine.core.source.file.FileSource
import java.awt.image.BufferedImage
import java.nio.ShortBuffer
import java.nio.file.Path
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * A [Decoder] that can decode [ImageContent] and [AudioContent] from a [Source] of [MediaType.VIDEO].
 *
 * Based on Jaffree FFmpeg wrapper, which spawns a new FFmpeg process for each [Source].
 *
 * @author Luca Rossetto
 * @author Ralph Gasser
 * @version 1.1.0
 */
class FFmpegVideoDecoder : DecoderFactory {

    override fun newDecoder(name: String, input: Enumerator, context: IndexContext): Decoder {
        val video = context[name, "video"]?.let { it.lowercase() == "true" } != false
        val audio = context[name, "audio"]?.let { it.lowercase() == "true" } != false
        val timeWindowMs = context[name, "timeWindowMs"]?.toLongOrNull() ?: 500L
        val ffmpegPath = context[name, "ffmpegPath"]?.let { Path.of(it) }

        return Instance(input, context, video, audio, timeWindowMs, ffmpegPath, name)
    }

    private class Instance(
        override val input: Enumerator,
        private val context: IndexContext,
        private val video: Boolean = true,
        private val audio: Boolean = true,
        private val timeWindowMs: Long = 500L,
        private val ffmpegPath: Path? = null,
        override val name: String
    ) : Decoder {

        /** [KLogger] instance. */
        private val logger: KLogger = KotlinLogging.logger {}

        private val ffprobe: FFprobe
            get() = if (this.ffmpegPath != null) FFprobe.atPath(this.ffmpegPath) else FFprobe.atPath()

        private val ffmpeg: FFmpeg
            get() = if (this.ffmpegPath != null) FFmpeg.atPath(this.ffmpegPath) else FFmpeg.atPath()

        override fun toFlow(scope: CoroutineScope): Flow<Retrievable> = channelFlow {
            val channel = this
            this@Instance.input.toFlow(scope).collect { retrievable ->
                /* Extract source. */
                val source = retrievable.filteredAttribute(SourceAttribute::class.java)?.source
                if (source?.type != MediaType.VIDEO) {
                    logger.debug { "Skipping retrievable ${retrievable.id} because it is not of type VIDEO." }
                    channel.send(retrievable)
                    return@collect
                }

                val probeResult = this@Instance.ffprobe.setShowStreams(true).also {
                    if (source is FileSource) {
                        it.setInput(source.path)
                    } else {
                        it.setInput(source.newInputStream())
                    }
                }.execute()

                /* Extract metadata. */
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

                /* Create consumer. */
                val consumer = InFlowFrameConsumer(channel, retrievable)

                /* Execute. */
                try {
                    var output = FrameOutput.withConsumerAlpha(consumer).disableStream(StreamType.SUBTITLE).disableStream(StreamType.DATA)
                    if (!this@Instance.video) {
                        output = output.disableStream(StreamType.VIDEO)
                    }
                    if (!this@Instance.audio) {
                        output = output.disableStream(StreamType.AUDIO)
                    }
                    if (source is FileSource) {
                        this@Instance.ffmpeg.addInput(UrlInput.fromPath(source.path)).addOutput(output).execute()
                    } else {
                        source.newInputStream().use {
                            this@Instance.ffmpeg.addInput(PipeInput.pumpFrom(it)).addOutput(output).execute()
                        }
                    }

                    /* Emit final frames. */
                    if (!consumer.isEmpty()) {
                        consumer.emit()
                    }

                    /* Emit source retrievable. */
                    send(retrievable)
                } catch (e: Throwable) {
                    logger.error(e) { "Error while decoding source ${source.name} (${source.sourceId})." }
                }
            }
        }.buffer(capacity = RENDEZVOUS, onBufferOverflow = BufferOverflow.SUSPEND).flowOn(Dispatchers.IO)


        /**
         * A [FrameConsumer] that emits [Retrievable]s to the downstream [channel].
         */
        private inner class InFlowFrameConsumer(private val channel: ProducerScope<Retrievable>, val source: Retrievable) : FrameConsumer {

            /** The video [Stream] processed by this [InFlowFrameConsumer]. */
            var videoStream: Stream? = null
                private set

            /** The audio [Stream] processed by this [InFlowFrameConsumer]. */
            var audioStream: Stream? = null
                private set

            /** The end of the time window. */
            var windowEnd  = TimeUnit.MILLISECONDS.toMicros(this@Instance.timeWindowMs)
                private set

            /** Flag indicating, that video is ready to be emitted. */
            var videoReady = false

            /** Flag indicating, that audio is ready to be emitted. */
            var audioReady = false

            /** [List] of grabbed [BufferedImage]s.  */
            val imageBuffer: List<Pair<BufferedImage,Long>> = LinkedList()

            /** [List] of grabbed [ShortBuffer]s.  */
            val audioBuffer: List<Pair<ShortBuffer,Long>> = LinkedList()

            /**
             * Returns true if both the image and audio buffer are empty.
             */
            fun isEmpty(): Boolean = this.imageBuffer.isEmpty() && this.audioBuffer.isEmpty()

            /**
             * Initializes this [InFlowFrameConsumer].
             *
             * @param streams List of [Stream]s to initialize the [InFlowFrameConsumer] with.
             */
            override fun consumeStreams(streams: MutableList<Stream>) {
                this.videoStream = streams.firstOrNull { it.type == Stream.Type.VIDEO }
                this.audioStream = streams.firstOrNull { it.type == Stream.Type.AUDIO }

                /* Reset counters and flags. */
                this@InFlowFrameConsumer.videoReady = !(this@InFlowFrameConsumer.videoStream != null && this@Instance.video)
                this@InFlowFrameConsumer.audioReady = !(this@InFlowFrameConsumer.audioStream != null && this@Instance.audio)
            }

            /**
             * Consumes a single [Frame].
             *
             * @param frame [Frame] to consume.
             */
            override fun consume(frame: Frame?) = runBlocking {
                if (frame == null) return@runBlocking
                val stream = when (frame.streamId) {
                    this@InFlowFrameConsumer.audioStream?.id -> this@InFlowFrameConsumer.audioStream!!
                    this@InFlowFrameConsumer.videoStream?.id -> this@InFlowFrameConsumer.videoStream!!
                    else -> return@runBlocking
                }
                val timestamp = ((1_000_000 * frame.pts) / stream.timebase)
                when (stream.type) {
                    Stream.Type.VIDEO -> {
                        (this@InFlowFrameConsumer.imageBuffer as LinkedList).add(frame.image!! to timestamp)
                        if (timestamp >= this@InFlowFrameConsumer.windowEnd) {
                            this@InFlowFrameConsumer.videoReady = true
                        }
                    }
                    Stream.Type.AUDIO -> {
                        val samples = ShortBuffer.wrap(frame.samples.map { (it shr 16).toShort() }.toShortArray())
                        (this@InFlowFrameConsumer.audioBuffer as LinkedList).add(samples to timestamp)
                        if (timestamp >= this@InFlowFrameConsumer.windowEnd) {
                            this@InFlowFrameConsumer.audioReady = true
                        }
                    }
                    else -> {}
                }

                /* If enough frames have been collected, emit them. */
                if (this@InFlowFrameConsumer.videoReady && this@InFlowFrameConsumer.audioReady) {
                    emit()

                    /* Reset counters and flags. */
                    this@InFlowFrameConsumer.videoReady = !(this@InFlowFrameConsumer.videoStream != null && this@Instance.video)
                    this@InFlowFrameConsumer.audioReady = !(this@InFlowFrameConsumer.audioStream != null && this@Instance.audio)

                    /* Update window end. */
                    this@InFlowFrameConsumer.windowEnd += TimeUnit.MILLISECONDS.toMicros(this@Instance.timeWindowMs)
                }
            }

            /**
             * Emits a single [Retrievable] to the downstream [channel].
             */
            suspend fun emit() {
                /* Audio samples. */
                var audioSize = 0
                val emitImage = mutableListOf<BufferedImage>()
                val emitAudio = mutableListOf<ShortBuffer>()

                /* Drain buffers. */
                (this.imageBuffer as LinkedList).removeIf {
                    if (it.second <= this.windowEnd) {
                        emitImage.add(it.first)
                        true
                    } else {
                        false
                    }
                }
                (this.audioBuffer as LinkedList).removeIf {
                    if (it.second <= this.windowEnd) {
                        audioSize += it.first.limit()
                        emitAudio.add(it.first)
                        true
                    } else {
                        false
                    }
                }

                /* Prepare attributes and content lists. */
                val attributes = mutableSetOf<RetrievableAttribute>()
                val content = mutableListOf<ContentElement<*>>()

                /* Prepare ingested with relationship to source. */
                val retrievableId = UUID.randomUUID()
                val source = this.source.filteredAttribute(SourceAttribute::class.java)
                val relationship = source?.let {
                    Relationship.ById(retrievableId, "partOf", it.source.sourceId, false)
                }

                /* Add time range. */
                attributes.add(
                    TimeRangeAttribute(
                        this.windowEnd - TimeUnit.MILLISECONDS.toMicros(this@Instance.timeWindowMs),
                        this.windowEnd,
                        TimeUnit.MICROSECONDS
                    )
                )

                /* Prepare and append audio content element. */
                if (emitAudio.isNotEmpty()) {
                    val samples = ShortBuffer.allocate(audioSize)
                    for (frame in emitAudio) {
                        frame.clear()
                        samples.put(frame)
                    }
                    samples.clear()
                    val audio = this@Instance.context.contentFactory.newAudioContent(
                        this.audioStream!!.channels.toShort(),
                        this.audioStream!!.sampleRate.toInt(),
                        samples
                    )
                    content.add(audio)
                }

                /* Prepare and append image content element. */
                for (image in emitImage) {
                    val imageContent = this@Instance.context.contentFactory.newImageContent(image)
                    content.add(imageContent)
                }

                logger.debug { "Emitting ingested $retrievableId with ${emitImage.size} images and ${emitAudio.size} audio samples." }

                /* Emit ingested. */
                this.channel.send(Ingested(retrievableId, "SEGMENT", content = content, attributes = attributes, relationships = relationship?.let { setOf(it) } ?: emptySet(), transient = false))
            }
        }
    }
}