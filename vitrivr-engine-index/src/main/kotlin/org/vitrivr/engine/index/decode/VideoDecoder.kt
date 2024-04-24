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
import org.vitrivr.engine.core.model.content.decorators.SourcedContent
import org.vitrivr.engine.core.model.content.element.AudioContent
import org.vitrivr.engine.core.model.content.element.ContentElement
import org.vitrivr.engine.core.model.content.element.ImageContent
import org.vitrivr.engine.core.operators.ingest.Decoder
import org.vitrivr.engine.core.operators.ingest.DecoderFactory
import org.vitrivr.engine.core.operators.ingest.Enumerator
import org.vitrivr.engine.core.source.MediaType
import org.vitrivr.engine.core.source.Metadata
import org.vitrivr.engine.core.source.Source
import java.nio.ShortBuffer


/**
 * A [Decoder] that can decode [ImageContent] and [AudioContent] from a [Source] of [MediaType.VIDEO].
 *
 * @author Fynn Firouz Faber
 * @author Ralph Gasser
 * @version 1.0.0
 */
class VideoDecoder : DecoderFactory {

    override fun newOperator(input: Enumerator, context: IndexContext, parameters: Map<String, String>): Decoder {
        val video = parameters["video"]?.let { it.lowercase() == "true" } ?: true
        val audio = parameters["audio"]?.let { it.lowercase() == "true" } ?: true
        val keyFrames = parameters["keyFrames"]?.let { it.lowercase() == "true" } ?: false
        val sampleVideo = parameters["sample.video"]?.toIntOrNull() ?: 1
        val sampleAudio = parameters["sample.audio"]?.toIntOrNull() ?: 1
        return Instance(input, context, video, audio, keyFrames, sampleVideo, sampleAudio)
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
        private val sampleVideo: Int = 1,
        private val sampleAudio: Int = 1,
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
        override fun toFlow(scope: CoroutineScope): Flow<ContentElement<*>> {
            val input = this@Instance.input.toFlow(scope).filter { it.type == MediaType.VIDEO }
            return channelFlow {
                val channel = this
                input.collect { source ->
                    var videoCounter = 0
                    var audioCounter = 0
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
                                var frame = grabber.grabFrame(this@Instance.audio, this@Instance.video, true, this@Instance.keyFrames, true)

                                while (frame != null) {
                                    when (frame.type) {
                                        Frame.Type.VIDEO -> if ((videoCounter++) % this@Instance.sampleVideo == 0) {
                                            emitImageContent(frame, source, channel)
                                        }

                                        Frame.Type.AUDIO -> if ((audioCounter++) % this@Instance.sampleAudio == 0) {
                                            emitAudioContent(frame, source, channel)
                                        }
                                        else -> {}
                                    }
                                    frame = grabber.grabFrame(this@Instance.audio, this@Instance.video, true, this@Instance.keyFrames, true)
                                }
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

        /**
         * Converts a [Frame] of type [Frame.Type.VIDEO] to an [ImageContent].
         *
         * @param frame The [Frame] to convert.
         * @param source The [Frame]'s [Source]
         * @param source The [ProducerScope]'s to send [ContentElement] to.
         */
        private suspend fun emitImageContent(frame: Frame, source: Source, channel: ProducerScope<ContentElement<*>>) {
            val image = Java2DFrameConverter().use {
                this.context.contentFactory.newImageContent(it.convert(frame))
            }
            val timestampNs: Long = frame.timestamp * 1000 // Convert microseconds to nanoseconds
            channel.send(object : ImageContent by image, SourcedContent.Temporal {
                override val source: Source = source
                override val timepointNs: Long = timestampNs
            })
        }

        /**
         * Converts a [Frame] of type [Frame.Type.AUDIO] to an [AudioContent].
         *
         * @param frame The [Frame] to convert.
         * @param source The [Frame]'s [Source]
         * @param source The [ProducerScope]'s to send [ContentElement] to.
         */
        private suspend fun emitAudioContent(frame: Frame, source: Source, channel: ProducerScope<ContentElement<*>>) {
            val timestampNs: Long = frame.timestamp * 1000 // Convert microseconds to nanoseconds

            /* Copy audio samples (important)! */
            val samples = (frame.samples.firstOrNull() as? ShortBuffer)?.let { ShortBuffer.allocate(it.limit()).put(it).flip() }
            if (samples == null) {
                logger.warn { "Audio frame at timestamp $timestampNs did not contain any audio samples." }
                return
            }

            /* Create and emit audio content. */
            val audio = this.context.contentFactory.newAudioContent(frame.audioChannels.toShort(), frame.sampleRate, samples)
            channel.send(object : AudioContent by audio, SourcedContent.Temporal {
                override val source: Source = source
                override val timepointNs: Long = timestampNs
            })
        }
    }
}