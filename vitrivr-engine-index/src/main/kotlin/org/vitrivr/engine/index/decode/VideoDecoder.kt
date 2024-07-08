package org.vitrivr.engine.index.decode

import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel.Factory.RENDEZVOUS
import kotlinx.coroutines.channels.ProducerScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.channelFlow
import org.bytedeco.javacv.FFmpegFrameGrabber
import org.bytedeco.javacv.Frame
import org.bytedeco.javacv.FrameGrabber
import org.bytedeco.javacv.Java2DFrameConverter
import org.vitrivr.engine.core.context.IndexContext
import org.vitrivr.engine.core.model.content.Content
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
import java.awt.image.BufferedImage
import java.nio.ShortBuffer
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * A [Decoder] that can decode [ImageContent] and [AudioContent] from a [Source] of [MediaType.VIDEO].
 *
 * @author Ralph Gasser
 * @version 2.1.0
 */
class VideoDecoder : DecoderFactory {

    override fun newDecoder(name: String, input: Enumerator, context: IndexContext): Decoder {
        val video = context[name, "video"]?.let { it.lowercase() == "true" } ?: true
        val audio = context[name, "audio"]?.let { it.lowercase() == "true" } ?: true
        val keyFrames = context[name, "keyFrames"]?.let { it.lowercase() == "true" } ?: false
        val timeWindowMs = context[name, "timeWindowMs"]?.toLongOrNull() ?: 500L
        return Instance(input, context, video, audio, keyFrames, timeWindowMs, name)
    }

    /**
     * The [Decoder] returned by this [VideoDecoder].
     */
    private class Instance(
        override val input: Enumerator,
        private val context: IndexContext,
        private val video: Boolean = true,
        private val audio: Boolean = true,
        private val keyFrames: Boolean = false,
        private val timeWindowMs: Long = 500L,
        private val name : String
    ) : Decoder {

        /** [KLogger] instance. */
        private val logger: KLogger = KotlinLogging.logger {}

        /**
         * Converts this [VideoDecoder] to a [Flow] of [Content] elements.
         *
         * Produces [ImageContent] and [AudioContent] elements.
         *
         * @param scope The [CoroutineScope] used for conversion.
         * @return [Flow] of [Content]
         */
        override fun toFlow(scope: CoroutineScope): Flow<Retrievable> = channelFlow {
            val channel = this
            this@Instance.input.toFlow(scope).collect { sourceRetrievable ->
                /* Extract source. */
                val source = sourceRetrievable.filteredAttribute(SourceAttribute::class.java)?.source ?: return@collect
                if (source.type != MediaType.VIDEO) {
                    logger.debug { "In flow: Skipping source ${source.name} (${source.sourceId}) because it is not of type VIDEO." }
                    return@collect
                }

                /* Decode video and audio; make distinction between FileSource and other types of sources. */
                if (source is FileSource) {
                    FFmpegFrameGrabber(source.path.toFile()).use { grabber ->
                        decodeFromGrabber(source, sourceRetrievable, grabber, channel)
                    }
                } else {
                    source.newInputStream().use { input ->
                        FFmpegFrameGrabber(input).use { grabber ->
                            decodeFromGrabber(source, sourceRetrievable, grabber, channel)
                        }
                    }
                }
            }
        }.buffer(capacity = RENDEZVOUS, onBufferOverflow = BufferOverflow.SUSPEND)


        /**
         * Decodes a video from a [FFmpegFrameGrabber] and emits [Retrievable] elements to the downstream [channel].
         *
         * @param source The [Source] from which the video is being decoded.
         * @param grabber The [FFmpegFrameGrabber] used to decode the video.
         * @param channel The [ProducerScope] used to emit [Retrievable] elements.
         */
        private suspend fun decodeFromGrabber(source: Source, sourceRetrievable: Retrievable, grabber: FFmpegFrameGrabber, channel: ProducerScope<Retrievable>) {
            /* Determine end of time window. */
            var windowEnd = TimeUnit.MILLISECONDS.toMicros(this@Instance.timeWindowMs)
            var error = false

            /* Configure FFmpegFrameGrabber. */
            grabber.imageMode = FrameGrabber.ImageMode.COLOR
            grabber.sampleMode = FrameGrabber.SampleMode.SHORT

            logger.info { "Start decoding source ${source.name} (${source.sourceId}): ${sourceRetrievable.id}" }
            try {
                grabber.start()

                /* Extract and enrich source metadata. */
                source.metadata[Metadata.METADATA_KEY_VIDEO_FPS] = grabber.videoFrameRate
                source.metadata[Metadata.METADATA_KEY_IMAGE_WIDTH] = grabber.imageWidth
                source.metadata[Metadata.METADATA_KEY_IMAGE_HEIGHT] = grabber.imageHeight
                source.metadata[Metadata.METADATA_KEY_AUDIO_CHANNELS] = grabber.audioChannels
                source.metadata[Metadata.METADATA_KEY_AUDIO_SAMPLERATE] = grabber.sampleRate
                source.metadata[Metadata.METADATA_KEY_AUDIO_SAMPLESIZE] = grabber.sampleFormat

                /* Start extraction of frames. */
                val imageBuffer = LinkedList<Pair<BufferedImage, Long>>()
                val audioBuffer = LinkedList<Pair<ShortBuffer, Long>>()

                /* Flags indicating that video / audio is ready to be emitted. */
                var videoReady = !(grabber.hasVideo() && this@Instance.video)
                var audioReady = !(grabber.hasAudio() && this@Instance.audio)

                do {
                    val frame = grabber.grabFrame(this@Instance.audio, this@Instance.video, true, this@Instance.keyFrames, true) ?: break
                    when (frame.type) {
                        Frame.Type.VIDEO -> {
                            imageBuffer.add(Java2DFrameConverter().use { it.convert(frame) to frame.timestamp })
                            if (frame.timestamp > windowEnd) {
                                videoReady = true
                            }
                        }

                        Frame.Type.AUDIO -> {
                            val samples = frame.samples.firstOrNull() as? ShortBuffer
                            if (samples != null) {
                                audioBuffer.add(ShortBuffer.allocate(samples.limit()).put(samples) to frame.timestamp)
                            }
                            if (frame.timestamp > windowEnd) {
                                audioReady = true
                            }
                        }

                        else -> { /* No op. */
                        }
                    }

                    /* If enough frames have been collected, emit them. */
                    if (videoReady && audioReady) {
                        emit(imageBuffer, audioBuffer, grabber, windowEnd, sourceRetrievable, channel)

                        /* Reset counters and flags. */
                        videoReady = !(grabber.hasVideo() && this@Instance.video)
                        audioReady = !(grabber.hasAudio() && this@Instance.audio)

                        /* Update window end. */
                        windowEnd += TimeUnit.MILLISECONDS.toMicros(this@Instance.timeWindowMs)
                    }
                } while (true)

                /* If there are frames left, then emit them. */
                if (audioBuffer.isNotEmpty() || imageBuffer.isNotEmpty()) {
                    emit(imageBuffer, audioBuffer, grabber, windowEnd, sourceRetrievable, channel)
                }

                logger.info { "Finished decoding video from source '${source.name}' (${source.sourceId}): ${sourceRetrievable.id}" }
            } catch (exception: Exception) {
                error = true
                logger.error(exception) { "Failed to decode video from source '${source.name}' (${source.sourceId})." }
            } finally {
                grabber.stop()
            }

            /* Send source retrievable downstream as a signal that file has been decoded. */
            if (!error) {
                channel.send(sourceRetrievable)
            }
        }

        /**
         * Emits a single [Retrievable] to the downstream [channel].
         *
         * @param imageBuffer A [LinkedList] containing [BufferedImage] elements to emit (frames).
         * @param audioBuffer The [LinkedList] containing the [ShortBuffer] elements to emit (audio samples).
         * @param grabber The [FrameGrabber] instance.
         * @param timestampEnd The end timestamp.
         * @param source The source [Retrievable] the emitted [Retrievable] is part of.
         */
        private suspend fun emit(imageBuffer: LinkedList<Pair<BufferedImage, Long>>, audioBuffer: LinkedList<Pair<ShortBuffer, Long>>, grabber: FrameGrabber, timestampEnd: Long, source: Retrievable, channel: ProducerScope<Retrievable>) {
            /* Audio samples. */
            var audioSize = 0
            val emitImage = mutableListOf<BufferedImage>()
            val emitAudio = mutableListOf<ShortBuffer>()

            /* Drain buffers. */
            imageBuffer.removeIf {
                if (it.second <= timestampEnd) {
                    emitImage.add(it.first)
                    true
                } else {
                    false
                }
            }
            audioBuffer.removeIf {
                if (it.second <= timestampEnd) {
                    audioSize += it.first.limit()
                    emitAudio.add(it.first)
                    true
                } else {
                    false
                }
            }

            /* Prepare ingested with relationship to source. */
            val ingested = Ingested(UUID.randomUUID(), "SEGMENT", false)
            source.filteredAttribute(SourceAttribute::class.java)?.let { ingested.addAttribute(it) }
            ingested.addRelationship(Relationship.ByRef(ingested, "partOf", source, false))
            ingested.addAttribute(TimeRangeAttribute(timestampEnd - TimeUnit.MILLISECONDS.toMicros(this@Instance.timeWindowMs), timestampEnd, TimeUnit.MICROSECONDS))

            /* Prepare and append audio content element. */
            if (emitAudio.size > 0) {
                val samples = ShortBuffer.allocate(audioSize)
                for (frame in emitAudio) {
                    frame.clear()
                    samples.put(frame)
                }
                samples.clear()
                val audio = this.context.contentFactory.newAudioContent(grabber.audioChannels.toShort(), grabber.sampleRate, samples)
                ingested.addContent(audio)
                ingested.addAttribute(ContentAuthorAttribute(audio.id, name))
            }

            /* Prepare and append image content element. */
            for (image in emitImage) {
                val imageContent = this.context.contentFactory.newImageContent(image)
                ingested.addContent(imageContent)
                ingested.addAttribute(ContentAuthorAttribute(imageContent.id, name))
            }

            logger.debug { "Emitting ingested ${ingested.id} with ${emitImage.size} images and ${emitAudio.size} audio samples: ${ingested.id}" }

            /* Emit ingested. */
            channel.send(ingested)
        }
    }
}
