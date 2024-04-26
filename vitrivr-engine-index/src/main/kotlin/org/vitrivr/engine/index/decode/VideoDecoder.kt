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
import kotlinx.coroutines.flow.filter
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
import org.vitrivr.engine.core.model.retrievable.attributes.SourceAttribute
import org.vitrivr.engine.core.model.retrievable.attributes.TimestampAttribute
import org.vitrivr.engine.core.operators.ingest.Decoder
import org.vitrivr.engine.core.operators.ingest.DecoderFactory
import org.vitrivr.engine.core.operators.ingest.Enumerator
import org.vitrivr.engine.core.source.MediaType
import org.vitrivr.engine.core.source.Metadata
import org.vitrivr.engine.core.source.Source
import java.nio.ShortBuffer
import java.util.*


/**
 * A [Decoder] that can decode [ImageContent] and [AudioContent] from a [Source] of [MediaType.VIDEO].
 *
 * @author Fynn Firouz Faber
 * @author Ralph Gasser
 * @version 2.0.0
 */
class VideoDecoder : DecoderFactory {

    override fun newDecoder(name: String, input: Enumerator, context: IndexContext): Decoder {
        val video = context[name, "video"]?.let { it.lowercase() == "true" } ?: true
        val audio = context[name, "audio"]?.let { it.lowercase() == "true" } ?: true
        val keyFrames = context[name, "keyFrames"]?.let { it.lowercase() == "true" } ?: false
        val timeWindowMs = context[name, "timeWindowMs"]?.toLongOrNull() ?: 500L
        return Instance(input, context, video, audio, keyFrames, timeWindowMs)
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
        override fun toFlow(scope: CoroutineScope): Flow<Ingested> {
            val input = this@Instance.input.toFlow(scope).filter { it.type == MediaType.VIDEO }
            return channelFlow {
                val channel = this
                input.collect { source ->
                    var windowEnd = this@Instance.timeWindowMs * 1000L

                    /* Create source ingested. */
                    val sourceIngested = Ingested(source.sourceId, source.type.toString(), false)
                    sourceIngested.addAttribute(SourceAttribute(source))

                    /* Decode video and audio. */
                    source.newInputStream().use { input ->
                        FFmpegFrameGrabber(input).use { grabber ->
                            /* Configure FFmpegFrameGrabber. */
                            grabber.imageMode = FrameGrabber.ImageMode.COLOR
                            grabber.sampleMode = FrameGrabber.SampleMode.SHORT

                            logger.info { "Start decoding source ${source.name} (${source.sourceId})" }
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
                                val buffer = LinkedList<Frame>()

                                /* Flags indicating that video / audio is ready to be emitted. */
                                var videoReady = !this@Instance.video
                                var audioReady = !this@Instance.audio

                                do {
                                    val frame = grabber.grabFrame(this@Instance.audio, this@Instance.video, true, this@Instance.keyFrames, true) ?: break
                                    when (frame.type) {
                                        Frame.Type.VIDEO -> {
                                            buffer.add(frame)
                                            if (frame.timestamp > windowEnd) {
                                                videoReady = true
                                            }
                                        }

                                        Frame.Type.AUDIO -> {
                                            buffer.add(frame)
                                            if (frame.timestamp > windowEnd) {
                                                audioReady = true
                                            }
                                        }

                                        else -> { /* No op. */
                                        }
                                    }

                                    /* If enough frames have been collected, emit them. */
                                    if (videoReady && audioReady) {
                                        emit(buffer, grabber, windowEnd, sourceIngested, channel)

                                        /* Reset counters and flags. */
                                        videoReady = !this@Instance.video
                                        audioReady = !this@Instance.audio

                                        /* Update window end. */
                                        windowEnd += this@Instance.timeWindowMs * 1000L
                                    }
                                } while (true)
                            } catch (exception: Exception) {
                                logger.error(exception) { "An error occurred while decoding video from source $source. Skipping..." }
                            } finally {
                                grabber.stop()
                                logger.info { "Finished decoding source ${source.name} (${source.sourceId})" }
                            }
                        }
                    }
                }
            }.buffer(capacity = RENDEZVOUS, onBufferOverflow = BufferOverflow.SUSPEND)
        }

        private suspend fun emit(buffer: LinkedList<Frame>, grabber: FrameGrabber, timestampEnd: Long, source: Ingested, channel: ProducerScope<Ingested>) {
            /* Video frames. */
            val video = mutableListOf<Frame>()

            /* Audio samples. */
            val sampleList = mutableListOf<ShortBuffer>()
            var audioSize = 0

            /* Drain buffer. */
            buffer.removeIf {
                when (it.type) {
                    Frame.Type.VIDEO -> {
                        if (it.timestamp < timestampEnd) {
                            video.add(it)
                            true
                        } else {
                            false
                        }
                    }

                    Frame.Type.AUDIO -> {
                        if (it.timestamp < timestampEnd) {
                            val sample = it.samples.firstOrNull() as? ShortBuffer
                            if (sample != null) {
                                sampleList.add(sample)
                                audioSize += sample.limit()
                            }
                            true
                        } else {
                            false
                        }
                    }

                    else -> false
                }
            }

            /* Prepare ingested with relationship to source. */
            val ingested = Ingested(UUID.randomUUID(), "SEGMENT", false)
            source.filteredAttribute<SourceAttribute>()?.let { ingested.addAttribute(it) }
            ingested.addRelationship(Relationship.ByRef(ingested, "partOf", source, false))
            ingested.addAttribute(TimestampAttribute(timestampEnd * 1000L))

            /* Prepare and append audio content element. */
            if (sampleList.size > 0) {
                val samples = ShortBuffer.allocate(audioSize)
                for (frame in sampleList) {
                    samples.put(frame)
                }
                val audio = this.context.contentFactory.newAudioContent(grabber.audioChannels.toShort(), grabber.sampleRate, samples)
                ingested.addContent(audio)
            }

            /* Prepare and append image content element. */
            if (video.size > 0) {
                val image = Java2DFrameConverter().use {
                    this.context.contentFactory.newImageContent(it.convert(video.last()))
                }
                ingested.addContent(image)
            }

            /* Emit ingested. */
            channel.send(ingested)
        }
    }
}
