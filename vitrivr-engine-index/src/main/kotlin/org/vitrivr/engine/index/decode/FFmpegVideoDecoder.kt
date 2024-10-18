package org.vitrivr.engine.index.decode

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
import kotlinx.coroutines.withContext
import org.bytedeco.javacv.FrameGrabber
import org.vitrivr.engine.core.context.IndexContext
import org.vitrivr.engine.core.model.relationship.Relationship
import org.vitrivr.engine.core.model.retrievable.Ingested
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.model.retrievable.attributes.ContentAuthorAttribute
import org.vitrivr.engine.core.model.retrievable.attributes.SourceAttribute
import org.vitrivr.engine.core.model.retrievable.attributes.time.TimeRangeAttribute
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.core.operators.ingest.Decoder
import org.vitrivr.engine.core.operators.ingest.DecoderFactory
import org.vitrivr.engine.core.operators.ingest.Enumerator
import org.vitrivr.engine.core.source.MediaType
import org.vitrivr.engine.core.source.Source
import org.vitrivr.engine.core.source.file.FileSource
import java.awt.image.BufferedImage
import java.nio.ShortBuffer
import java.nio.file.Path
import java.util.*
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit

class FFmpegVideoDecoder : DecoderFactory {

    override fun newDecoder(name: String, input: Enumerator, context: IndexContext): Decoder {
        val timeWindowMs = context[name, "timeWindowMs"]?.toLongOrNull() ?: 500L
        val ffmpegPath = context[name, "ffmpegPath"]?.let { Path.of(it) }

        return Instance(input, context, timeWindowMs, name, ffmpegPath)
    }

    private class Instance(
        override val input: Enumerator,
        private val context: IndexContext,
        private val timeWindowMs: Long = 500L,
        private val name: String,
        private val ffmpegPath: Path?
    ) : Decoder {

        /** [KLogger] instance. */
        private val logger: KLogger = KotlinLogging.logger {}

//        private val ffprobe: FFprobe
//            get() = if (ffmpegPath != null) FFprobe.atPath(this.ffmpegPath) else FFprobe.atPath()

        private val ffmpeg: FFmpeg
            get() = if (ffmpegPath != null) FFmpeg.atPath(this.ffmpegPath) else FFmpeg.atPath()

        override fun toFlow(scope: CoroutineScope): Flow<Retrievable> = channelFlow<Retrievable> {
            this@Instance.input.toFlow(scope).collect { sourceRetrievable ->
                /* Extract source. */
                val source = sourceRetrievable.filteredAttribute(SourceAttribute::class.java)?.source ?: return@collect
                if (source.type != MediaType.VIDEO) {
                    logger.debug { "In flow: Skipping source ${source.name} (${source.sourceId}) because it is not of type VIDEO." }
                    return@collect
                }

                var windowEnd = TimeUnit.MILLISECONDS.toMicros(this@Instance.timeWindowMs)

                val imageBuffer = LinkedBlockingQueue<Pair<BufferedImage, Long>>()

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
                                        imageBuffer.add(frame.image!! to (1000 * frame.pts) / stream.timebase)
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
                )

                //TODO scaling
                //TODO audio settings

                val future = ffmpegInstance.executeAsync()

                while (!future.isDone || !future.isCancelled) {

                    if (imageBuffer.isNotEmpty() && imageBuffer.last().second >= windowEnd) {
                        emit(
                            imageBuffer, windowEnd, sourceRetrievable, this@channelFlow
                        )
                        windowEnd += TimeUnit.MILLISECONDS.toMicros(this@Instance.timeWindowMs)
                    } else {
                        Thread.yield()
                    }

                }

                if (imageBuffer.isNotEmpty()) {
                    emit(imageBuffer, windowEnd, sourceRetrievable, this@channelFlow)
                }

            }
        }.buffer(capacity = RENDEZVOUS, onBufferOverflow = BufferOverflow.SUSPEND)


        /**
         * Emits a single [Retrievable] to the downstream [channel].
         *
         * @param imageBuffer A [LinkedList] containing [BufferedImage] elements to emit (frames).
         * @param audioBuffer The [LinkedList] containing the [ShortBuffer] elements to emit (audio samples).
         * @param grabber The [FrameGrabber] instance.
         * @param timestampEnd The end timestamp.
         * @param source The source [Retrievable] the emitted [Retrievable] is part of.
         */
        private suspend fun emit(
            imageBuffer: LinkedBlockingQueue<Pair<BufferedImage, Long>>,
            //audioBuffer: LinkedList<Pair<ShortBuffer, Long>>,
            timestampEnd: Long,
            source: Retrievable,
            channel: ProducerScope<Retrievable>
        ) {
            /* Audio samples. */
            var audioSize = 0
            val emitImage = mutableListOf<BufferedImage>()
            //val emitAudio = mutableListOf<ShortBuffer>()

            /* Drain buffers. */
            imageBuffer.removeIf {
                if (it.second <= timestampEnd) {
                    emitImage.add(it.first)
                    true
                } else {
                    false
                }
            }
//            audioBuffer.removeIf {
//                if (it.second <= timestampEnd) {
//                    audioSize += it.first.limit()
//                    emitAudio.add(it.first)
//                    true
//                } else {
//                    false
//                }
//            }

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

//            /* Prepare and append audio content element. */
//            if (emitAudio.size > 0) {
//                val samples = ShortBuffer.allocate(audioSize)
//                for (frame in emitAudio) {
//                    frame.clear()
//                    samples.put(frame)
//                }
//                samples.clear()
//                val audio = this.context.contentFactory.newAudioContent(
//                    grabber.audioChannels.toShort(),
//                    grabber.sampleRate,
//                    samples
//                )
//                ingested.addContent(audio)
//                ingested.addAttribute(ContentAuthorAttribute(audio.id, name))
//            }

            /* Prepare and append image content element. */
            for (image in emitImage) {
                val imageContent = this.context.contentFactory.newImageContent(image)
                ingested.addContent(imageContent)
                ingested.addAttribute(ContentAuthorAttribute(imageContent.id, name))
            }

            //logger.debug { "Emitting ingested ${ingested.id} with ${emitImage.size} images and ${emitAudio.size} audio samples: ${ingested.id}" }

            /* Emit ingested. */
            channel.send(ingested)
        }

    }
}