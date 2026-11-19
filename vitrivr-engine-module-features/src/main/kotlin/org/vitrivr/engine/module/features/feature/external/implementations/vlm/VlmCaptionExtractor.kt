package org.vitrivr.engine.module.features.feature.external.implementations.vlm

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.takeWhile
import kotlinx.coroutines.runInterruptible
import org.vitrivr.engine.core.model.content.element.ContentElement
import org.vitrivr.engine.core.model.descriptor.struct.VlmDescriptionDescriptor
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.model.retrievable.TerminalRetrievable
import org.vitrivr.engine.core.model.retrievable.attributes.DescriptorAuthorAttribute
import org.vitrivr.engine.core.model.types.Value
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.core.operators.ingest.Extractor
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.util.UUID

/**
 * Generates and persists one caption per retrievable in the configured field.
 * If generation fails, the retrievable continues through ingestion and any stored caption is preserved.
 *
 * @author Rahel Arnold
 */
internal class VlmCaptionExtractor(
    override val input: Operator<out Retrievable>,
    override val analyser: VlmCaption,
    override val field: Schema.Field<ContentElement<*>, VlmDescriptionDescriptor>?,
    override val name: String,
    private val describe: ((Retrievable) -> VlmDescription?)?,
    private val concurrency: Int
) : Extractor<ContentElement<*>, VlmDescriptionDescriptor> {
    private val logger = KotlinLogging.logger("Extractor#$name")
    private val writer by lazy { field?.getWriter() }

    override fun toFlow(scope: CoroutineScope) = if (describe == null) {
        input.toFlow(scope)
    } else flow {
        coroutineScope {
            val pending = ArrayDeque<Deferred<Retrievable>>(concurrency)
            var terminated = false
            input.toFlow(scope)
                .takeWhile { shot ->
                    terminated = shot == TerminalRetrievable
                    !terminated
                }
                .collect { shot ->
                    pending.addLast(async {
                        runInterruptible(Dispatchers.IO) { extract(shot, describe) }
                    })
                    // Preserve source order for downstream video-level ASR.
                    if (pending.size == concurrency) emit(pending.removeFirst().await())
                }
            while (pending.isNotEmpty()) emit(pending.removeFirst().await())
            if (terminated) emit(TerminalRetrievable)
        }
    }

    private fun extract(shot: Retrievable, describe: (Retrievable) -> VlmDescription?): Retrievable {
        val description = try {
            describe(shot) ?: return shot
        } catch (e: IOException) {
            logger.warn(e) { "Could not generate $name for shot ${shot.id}." }
            return shot
        }
        val id = UUID.nameUUIDFromBytes("VlmCaption:$name:${shot.id}".toByteArray(StandardCharsets.UTF_8))
        val descriptor = VlmDescriptionDescriptor(id, shot.id, mapOf(
            VlmDescriptionDescriptor.CAPTION to Value.Text(description.text),
            "model" to Value.Text(description.model),
            "prompt_version" to Value.Text(description.promptVersion),
            "generation_metadata" to Value.Text(description.generationMetadata.toString())
        ), field)
        check(writer?.upsert(descriptor) != false) { "Could not persist $name for shot ${shot.id}." }
        val author = DescriptorAuthorAttribute().apply { add(descriptor, name) }
        return shot.copy(
            descriptors = shot.descriptors.filterNot { it.id == id }.toSet() + descriptor,
            attributes = shot.attributes + author
        )
    }
}
