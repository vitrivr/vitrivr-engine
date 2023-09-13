package org.vitrivr.engine.index.decode

import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.ProducerScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.filter
import org.bytedeco.javacv.FFmpegFrameGrabber
import org.bytedeco.javacv.Frame
import org.bytedeco.javacv.Java2DFrameConverter
import org.vitrivr.engine.core.model.content.AudioContent
import org.vitrivr.engine.core.model.content.Content
import org.vitrivr.engine.core.model.content.ImageContent
import org.vitrivr.engine.core.model.content.SourcedContent
import org.vitrivr.engine.core.model.content.impl.InMemoryAudioContent
import org.vitrivr.engine.core.model.content.impl.InMemoryImageContent
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.core.operators.ingest.Decoder
import org.vitrivr.engine.core.source.MediaType
import org.vitrivr.engine.core.source.Source
import java.awt.image.BufferedImage
import java.nio.ShortBuffer

/** [KLogger] instance. */
private val logger: KLogger = KotlinLogging.logger {}

/**
 * A [Decoder] that can decode [ImageContent] and [AudioContent] from a [Source] of [MediaType.VIDEO].
 *
 * @author Fynn Firouz Faber
 * @author Ralph Gasser
 * @version 1.0.0
 */
class VideoDecoder(override val input: Operator<Source>, private val video: Boolean = true, private val audio: Boolean = true) : Decoder {

    /** The [Java2DFrameConverter] used by this [VideoDecoder] instance. */
    private val converter: Java2DFrameConverter by lazy { Java2DFrameConverter() }

    /**
     * Converts this [VideoDecoder] to a [Flow] of [Content] elements.
     *
     * Produces [ImageContent] and [AudioContent] elements.
     *
     * @param scope The [CoroutineScope] used for conversion.
     * @return [Flow] of [Content]
     */
    override fun toFlow(scope: CoroutineScope): Flow<Content<*>> {
        val input = this@VideoDecoder.input.toFlow(scope).filter { it.type == MediaType.VIDEO }
        return channelFlow {
            val channel = this
            input.collect { source ->
                val grabber = FFmpegFrameGrabber(source.inputStream, 0)
                try {
                    grabber.start()
                    var frame = grabber.grabFrame(this@VideoDecoder.video, this@VideoDecoder.audio, true, false, true)
                    while (frame != null) {
                        when(frame.type) {
                            Frame.Type.VIDEO -> emitImageContent(frame, source, channel)
                            Frame.Type.AUDIO -> emitAudioContent(frame, source, channel)
                            //Frame.Type.SUBTITLE -> TODO
                            else -> {}
                        }
                        frame = grabber.grabFrame(this@VideoDecoder.video, this@VideoDecoder.audio, true, false, true)
                    }
                } catch (exception: Exception) {
                    logger.error(exception) { "An error occurred while decoding video from source $source. Skipping..." }
                } finally {
                    grabber.stop()
                    source.close()
                }
            }
        }
    }

    /**
     * Converts a [Frame] of type [Frame.Type.VIDEO] to an [ImageContent].
     *
     * @param frame The [Frame] to convert.
     * @param source The [Frame]'s [Source]
     * @param source The [ProducerScope]'s to send [VideoFrameContent] to.
     */
    private suspend fun emitImageContent(frame: Frame, source: Source, channel: ProducerScope<Content<*>>) {
        val image = InMemoryImageContent(this.converter.convert(frame)) /* TODO: Obtain cached version through factory. */
        val timestampNs: Long = frame.timestamp * 1000 // Convert microseconds to nanoseconds
        channel.send(VideoFrameContent(image, source, timestampNs))
    }

    /**
     * Converts a [Frame] of type [Frame.Type.AUDIO] to an [AudioContent].
     *
     * @param frame The [Frame] to convert.
     * @param source The [Frame]'s [Source]
     * @param source The [ProducerScope]'s to send [VideoFrameContent] to.
     */
    private suspend fun emitAudioContent(frame: Frame, source: Source, channel: ProducerScope<Content<*>>) {
        for ((c, s) in frame.samples.withIndex()) {
            val normalizedSamples = when (s)  {
                is ShortBuffer -> s
                else -> { ShortBuffer.allocate(0)/* TODO: Cover other cases. */ }
            }
            val timestampNs: Long = frame.timestamp * 1000 // Convert microseconds to nanoseconds
            val audio = InMemoryAudioContent(c, frame.sampleRate, normalizedSamples) /* TODO: Obtain cached version through factory. */
            channel.send(AudioFrameContent(audio, source, timestampNs))
        }
    }

    /**
     * An internal class that represents a single frame of a video.
     *
     * @see ImageContent
     * @see SourcedContent.Temporal
     */
    class VideoFrameContent(image: ImageContent, override val source: Source, override val timepointNs: Long): ImageContent by image, SourcedContent.Temporal<BufferedImage>

    /**
     * An internal class that represents a single frame of a video.
     *
     * @see AudioContent
     * @see SourcedContent.Temporal
     */
    class AudioFrameContent(audio: AudioContent, override val source: Source, override val timepointNs: Long): AudioContent by audio, SourcedContent.Temporal<ShortBuffer>
}