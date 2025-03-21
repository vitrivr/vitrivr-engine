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
import kotlinx.coroutines.runBlocking
import org.vitrivr.engine.core.context.IndexContext
import org.vitrivr.engine.core.model.content.element.AudioContent
import org.vitrivr.engine.core.model.content.element.ImageContent
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
import org.vitrivr.engine.core.source.Metadata
import org.vitrivr.engine.core.source.Source
import org.vitrivr.engine.core.source.file.FileSource
import org.vitrivr.engine.core.util.extension.loadServiceForName
import org.vitrivr.engine.index.util.boundaryFile.MediaSegmentDescriptor
import org.vitrivr.engine.index.util.boundaryFile.ShotBoundaryProvider
import org.vitrivr.engine.index.util.boundaryFile.ShotBoundaryProviderFactory
import java.awt.image.BufferedImage
import java.nio.ShortBuffer
import java.nio.file.Path
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * A [Decoder] that can decode [ImageContent] and [AudioContent] from a [Source] of [MediaType.VIDEO].
 * Further it uses the [ShotBoundaryProvider] to split the video into segments.
 *
 * Based on Jaffree FFmpeg wrapper, which spawns a new FFmpeg process for each [Source].
 *
 * @author Raphael Waltenspül
 * @author Luca Rossetto
 * @author Ralph Gasser
 * @version 1.0.0
 */
class ShotBoundaryFFmpegVideoDecoder : DecoderFactory {

    override fun newDecoder(name: String, input: Enumerator, context: IndexContext): Decoder {
        val video = context[name, "video"]?.let { it.lowercase() == "true" } != false
        val audio = context[name, "audio"]?.let { it.lowercase() == "true" } != false
        val timeWindowMs = context[name, "timeWindowMs"]?.toLongOrNull() ?: 500L
        val ffmpegPath = context[name, "ffmpegPath"]?.let { Path.of(it) }
        val sbProvider = context[name, "sbProvider"]?.let { it }
            ?: throw IllegalArgumentException("ShotBoundaryProvider not specified for $name")
        val sbName = context[name, "sbName"]?.let { it }
            ?: throw IllegalArgumentException("ShotBoundaryProvider not specified for $name")
        return Instance(input, context, video, audio, timeWindowMs, ffmpegPath, sbProvider, sbName, name)
    }

    private class Instance(
        override val input: Enumerator,
        private val context: IndexContext,
        private val video: Boolean = true,
        private val audio: Boolean = true,
        private val timeWindowMs: Long = 500L,
        private val ffmpegPath: Path? = null,
        private val sbProvider: String,
        private val sbName: String,
        override val name: String
    ) : Decoder {

        /** [KLogger] instance. */
        private val logger: KLogger = KotlinLogging.logger {}

        private val ffprobe: FFprobe
            get() = if (this.ffmpegPath != null) FFprobe.atPath(this.ffmpegPath) else FFprobe.atPath()

        private val ffmpeg: FFmpeg
            get() = if (this.ffmpegPath != null) FFmpeg.atPath(this.ffmpegPath) else FFmpeg.atPath()

        val factory = loadServiceForName<ShotBoundaryProviderFactory>(sbProvider)
            ?: throw IllegalArgumentException("Failed to find ShotBoundaryProviderFactory for $name")
        private val sb: ShotBoundaryProvider = factory.newShotBoundaryProvider(sbName, context)

        override fun toFlow(scope: CoroutineScope): Flow<Retrievable> = channelFlow {
            this@Instance.input.toFlow(scope).collect { sourceRetrievable ->
                /* Extract source. */
                val source = sourceRetrievable.filteredAttribute(SourceAttribute::class.java)?.source ?: return@collect
                if (source.type != MediaType.VIDEO) {
                    logger.debug { "In flow: Skipping source ${source.name} (${source.sourceId}) because it is not of type VIDEO." }
                    return@collect
                }

                /* Load shot boundaries. */
                // TODO parse/create  path/uri from source in a generic pattern.

                var uri = when (source) {
                    is FileSource -> source.path.toUri().toString()
                    else -> "Source $source is not a FileSource.".let {
                        logger.error { it }; throw IllegalArgumentException(it)
                    }
                }

                uri = "http://local-nmr.xreco-retrieval.ch/api/assets/resource/f58389e0-4f0e-42cd-800e-6f9ec2eeb2e5/asset.mp4"

                val sbs = sb.decode(uri)
                sbs.isEmpty().let {
                    if (it) {
                        logger.warn { "No shot boundaries found for source ${source.name} (${source.sourceId}) using fixed time window of $timeWindowMs ms." }
                    }
                }


                val probeResult = ffprobe.setShowStreams(true).also {
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
                    source.metadata[Metadata.METADATA_KEY_VIDEO_BITRATE] = videoStreamInfo.bitRate

                }

                val audioStreamInfo = probeResult.streams.find { it.codecType == StreamType.AUDIO }
                if (audioStreamInfo != null) {
                    source.metadata[Metadata.METADATA_KEY_AUDIO_CHANNELS] = audioStreamInfo.channels
                    source.metadata[Metadata.METADATA_KEY_AUDIO_SAMPLERATE] = audioStreamInfo.sampleRate
                    source.metadata[Metadata.METADATA_KEY_AUDIO_SAMPLESIZE] = audioStreamInfo.sampleFmt
                    source.metadata[Metadata.METADATA_KEY_AUDIO_BITRATE] = audioStreamInfo.bitRate
                }

                /* Create consumer. */
                val consumer = InFlowFrameConsumer(this, sourceRetrievable, sbs)

                /* Execute. */
                try {
                    var output = FrameOutput.withConsumerAlpha(consumer).disableStream(StreamType.SUBTITLE)
                        .disableStream(StreamType.DATA)
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
                    send(sourceRetrievable)
                } catch (e: Throwable) {
                    logger.error(e) { "Error while decoding source ${source.name} (${source.sourceId})." }
                }
            }
        }.buffer(capacity = RENDEZVOUS, onBufferOverflow = BufferOverflow.SUSPEND)


        /**
         * A [FrameConsumer] that emits [Retrievable]s to the downstream [channel].
         */
        private inner class InFlowFrameConsumer(
            private val channel: ProducerScope<Retrievable>,
            val source: Retrievable,
            val sbs: List<MediaSegmentDescriptor>
        ) : FrameConsumer {

            /** The video [Stream] processed by this [InFlowFrameConsumer]. */
            var videoStream: Stream? = null
                private set

            /** The audio [Stream] processed by this [InFlowFrameConsumer]. */
            var audioStream: Stream? = null
                private set

            val sbsIterator = sbs.iterator()

            var windowStartEnd = sbsIterator.hasNext().let {
                if (it) {
                    val next = sbsIterator.next()
                    Pair(
                        TimeUnit.MILLISECONDS.toMicros(next.startAbs.toMillis()),
                        TimeUnit.MILLISECONDS.toMicros(next.endAbs.toMillis())
                    )
                } else {
                    Pair(
                        TimeUnit.MILLISECONDS.toMicros(
                            0
                        ),
                        TimeUnit.MILLISECONDS.toMicros(
                            this@Instance.timeWindowMs
                        )
                    )
                }
            }
                private set

            /** Flag indicating, that video is ready to be emitted. */
            var videoReady = false

            /** Flag indicating, that audio is ready to be emitted. */
            var audioReady = false

            /** [List] of grabbed [BufferedImage]s.  */
            val imageBuffer: List<Pair<BufferedImage, Long>> = LinkedList()

            /** [List] of grabbed [ShortBuffer]s.  */
            val audioBuffer: List<Pair<ShortBuffer, Long>> = LinkedList()

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
                this@InFlowFrameConsumer.videoReady =
                    !(this@InFlowFrameConsumer.videoStream != null && this@Instance.video)
                this@InFlowFrameConsumer.audioReady =
                    !(this@InFlowFrameConsumer.audioStream != null && this@Instance.audio)
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
                //
                val timestamp = ((1_000_000 * frame.pts) / stream.timebase)
                when (stream.type) {
                    Stream.Type.VIDEO -> {

                        if (this@InFlowFrameConsumer.windowStartEnd.first <= timestamp && timestamp <= this@InFlowFrameConsumer.windowStartEnd.second) {
                            (this@InFlowFrameConsumer.imageBuffer as LinkedList).add(frame.image!! to timestamp)
                        }
                        if (timestamp >= this@InFlowFrameConsumer.windowStartEnd.second) {
                            this@InFlowFrameConsumer.videoReady = true
                        }
                    }

                    Stream.Type.AUDIO -> {
                        val samples = ShortBuffer.wrap(frame.samples.map { (it shr 16).toShort() }.toShortArray())
                        if (this@InFlowFrameConsumer.windowStartEnd.first <= timestamp && timestamp <= this@InFlowFrameConsumer.windowStartEnd.second) {
                            (this@InFlowFrameConsumer.audioBuffer as LinkedList).add(samples to timestamp)
                        }
                        if (timestamp >= this@InFlowFrameConsumer.windowStartEnd.second) {
                            this@InFlowFrameConsumer.audioReady = true
                        }
                    }

                    else -> {}
                }

                /* If enough frames have been collected, emit them. */
                if (this@InFlowFrameConsumer.videoReady && this@InFlowFrameConsumer.audioReady) {
                    emit()

                    /* Reset counters and flags. */
                    this@InFlowFrameConsumer.videoReady =
                        !(this@InFlowFrameConsumer.videoStream != null && this@Instance.video)
                    this@InFlowFrameConsumer.audioReady =
                        !(this@InFlowFrameConsumer.audioStream != null && this@Instance.audio)

                    /* Update window end. */
                    if (sbsIterator.hasNext()) {
                        val next = sbsIterator.next()
                        windowStartEnd = Pair(
                            TimeUnit.MILLISECONDS.toMicros(next.startAbs.toMillis()),
                            TimeUnit.MILLISECONDS.toMicros(next.endAbs.toMillis())
                        )
                    } else {
                        windowStartEnd = Pair(
                            windowStartEnd.second,
                            windowStartEnd.second + TimeUnit.MILLISECONDS.toMicros(this@Instance.timeWindowMs)
                        )
                    }
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
                    if (it.second <= this.windowStartEnd.second) {
                        emitImage.add(it.first)
                        true
                    } else {
                        false
                    }
                }
                (this.audioBuffer as LinkedList).removeIf {
                    if (it.second <= this.windowStartEnd.second) {
                        audioSize += it.first.limit()
                        emitAudio.add(it.first)
                        true
                    } else {
                        false
                    }
                }

                /* Prepare ingested with relationship to source. */
                val ingested = Ingested(UUID.randomUUID(), "SEGMENT", false)
                this.source.filteredAttribute(SourceAttribute::class.java)?.let { ingested.addAttribute(it) }
                ingested.addRelationship(Relationship.ByRef(ingested, "partOf", source, false))
                ingested.addAttribute(
                    TimeRangeAttribute(
                        windowStartEnd.first,
                        this.windowStartEnd.second,
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
                    ingested.addContent(audio)
                    ingested.addAttribute(ContentAuthorAttribute(audio.id, name))
                }

                /* Prepare and append image content element. */
                for (image in emitImage) {
                    val imageContent = this@Instance.context.contentFactory.newImageContent(image)
                    ingested.addContent(imageContent)
                    ingested.addAttribute(ContentAuthorAttribute(imageContent.id, name))
                }

                logger.debug { "Emitting ingested ${ingested.id} with ${emitImage.size} images and ${emitAudio.size} audio samples: ${ingested.id}" }

                /* Emit ingested. */
                this.channel.send(ingested)
            }
        }
    }
}