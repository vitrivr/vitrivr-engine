package org.vitrivr.engine.module.features.feature.external.implementations.asr

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.runInterruptible
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.vitrivr.engine.core.features.AbstractExtractor
import org.vitrivr.engine.core.model.content.ContentType
import org.vitrivr.engine.core.model.content.element.AudioContent
import org.vitrivr.engine.core.model.content.element.ContentElement
import org.vitrivr.engine.core.model.descriptor.scalar.TextDescriptor
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.model.retrievable.attributes.SourceAttribute
import org.vitrivr.engine.core.model.retrievable.attributes.time.ShotAudioRangeAttribute
import org.vitrivr.engine.core.model.types.Value
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.core.source.file.FileSource
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration
import java.util.UUID

/**
 * [AutomaticSpeechRecognitionExtractor] implementation of an [AbstractExtractor] for [AutomaticSpeechRecognition].
 *
 * This extractor sends audio content to the external ASR service (our Flask/APIFlask Whisper endpoint)
 * and creates a [TextDescriptor] containing the transcribed text.
 *
 * @author Rahel Arnold
 * @version 1.0.0
 */
class AutomaticSpeechRecognitionExtractor : AbstractExtractor<ContentElement<*>, TextDescriptor> {

    private val host: String
    private val continuousVideo: Boolean

    constructor(
        input: Operator<out Retrievable>,
        analyser: AutomaticSpeechRecognition,
        field: Schema.Field<ContentElement<*>, TextDescriptor>,
        host: String,
        continuousVideo: Boolean = false
    ) : super(input, analyser, field) {
        this.host = host
        this.continuousVideo = continuousVideo
    }

    constructor(
        input: Operator<out Retrievable>,
        analyser: AutomaticSpeechRecognition,
        name: String,
        host: String,
        continuousVideo: Boolean = false
    ) : super(input, analyser, name) {
        this.host = host
        this.continuousVideo = continuousVideo
    }

    override fun toFlow(scope: CoroutineScope): Flow<Retrievable> {
        if (!continuousVideo) return super.toFlow(scope)
        return flow {
            var currentVideo: FileSource? = null
            var words = emptyList<TimedWord>()
            input.toFlow(scope).collect { retrievable ->
                val range = retrievable.filteredAttribute(ShotAudioRangeAttribute::class.java)
                val source = retrievable.filteredAttribute(SourceAttribute::class.java)?.source
                if (range == null || source !is FileSource) {
                    emit(retrievable)
                    return@collect
                }

                if (source != currentVideo) {
                    words = try {
                        runInterruptible(Dispatchers.IO) { transcribeVideo(source) }
                    } catch (e: CancellationException) {
                        throw e
                    } catch (e: Exception) {
                        logger.error(e) { "Continuous ASR failed for ${source.name}" }
                        emptyList()
                    }
                    currentVideo = source
                }

                // Find the insertion point before every word at the shot start.
                val first = words.binarySearch { if (it.midpointNs < range.startNs) -1 else 1 }
                    .let { if (it < 0) -it - 1 else it }
                val text = words.subList(first, words.size).asSequence()
                    .takeWhile { it.midpointNs < range.endExclusiveNs }
                    .joinToString("") { it.text }
                    .trim()
                val descriptors = if (text.isEmpty()) emptyList()
                    else listOf(TextDescriptor(UUID.randomUUID(), retrievable.id, Value.Text(text), field))
                emit(persistDescriptors(retrievable, descriptors))
            }
        }
    }

    private fun transcribeVideo(source: FileSource): List<TimedWord> {
        val request = HttpRequest.newBuilder(URI.create("${host.trimEnd('/')}/extract/asr/video"))
            .timeout(Duration.ofMinutes(15))
            .header("Content-Type", "application/octet-stream")
            .POST(HttpRequest.BodyPublishers.ofFile(source.path))
            .build()
        val response = videoClient.send(request, HttpResponse.BodyHandlers.ofString())
        check(response.statusCode() == 200) { "ASR video endpoint returned HTTP ${response.statusCode()}" }
        return Json.decodeFromString<List<TimedWord>>(response.body()).sortedBy { it.midpointNs }
    }

    @Serializable
    private data class TimedWord(val midpointNs: Long, val text: String)

    private companion object {
        val videoClient: HttpClient by lazy {
            HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build()
        }
    }

    /**
     * Determines if this extractor can process the given [Retrievable].
     */
    override fun matches(retrievable: Retrievable): Boolean =
        retrievable.content.any { it.type == ContentType.AUDIO_FRAME }

    /**
     * Performs ASR extraction on all [AudioContent] elements within a [Retrievable].
     */
    override fun extract(retrievable: Retrievable) =
        retrievable.content
            .filterIsInstance<AudioContent>()
            .map { audioContent ->
                // Send base64 audio (data:audio/wav;base64,...) to /extract/asr
                AutomaticSpeechRecognition.analyse(audioContent, this.host)
                    .copy(
                        retrievableId = retrievable.id,
                        field = this@AutomaticSpeechRecognitionExtractor.field
                    )
            }
}
