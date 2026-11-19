package org.vitrivr.engine.module.features.feature.external.implementations.vlm

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import org.vitrivr.engine.core.context.Context
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.model.retrievable.TerminalRetrievable
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.core.operators.OperatorFactory
import java.io.IOException
import javax.imageio.ImageIO

/**
 * Reads stored keyframes by persisted shot ID for caption-only indexing.
 *
 * @author Rahel Arnold
 */
class StoredShotKeyframes : OperatorFactory {
    override fun newOperator(
        name: String, inputs: Map<String, Operator<out Retrievable>>, context: Context
    ): Operator<Retrievable> {
        require(inputs.isEmpty()) { "StoredShotKeyframes must be the first pipeline operation." }
        val resolverName = requireNotNull(context[name, "resolver"]) { "StoredShotKeyframes requires resolver." }
        val resolver = requireNotNull(context.resolver[resolverName]) { "Unknown keyframe resolver: $resolverName." }
        val shotType = requireNotNull(context[name, "shotType"]) { "StoredShotKeyframes requires shotType." }
        val suffix = context[name, "suffix"] ?: "png"
        require(suffix in setOf("png", "jpeg", "jpg")) { "StoredShotKeyframes supports png, jpeg and jpg." }

        return object : Operator.Nullary<Retrievable> {
            override val name = name

            override fun toFlow(scope: CoroutineScope) = flow {
                val reader = context.schema.connection.getRetrievableReader()
                for (shot in reader.getAll().filter { it.type == shotType }) {
                    val resource = resolver.resolve(shot.id, suffix)
                    if (resource == null || !resource.exists()) continue
                    val image = try {
                        resource.openInputStream().use { ImageIO.read(it) }
                    } catch (e: IOException) {
                        logger.warn(e) { "Could not read stored keyframe for ${shot.id}." }
                        continue
                    }
                    if (image == null) {
                        logger.warn { "Invalid stored keyframe for ${shot.id}." }
                        continue
                    }
                    emit(shot.copy(content = listOf(context.contentFactory.newImageContent(image))))
                }
                emit(TerminalRetrievable)
            }.flowOn(Dispatchers.IO)
        }
    }

    private companion object {
        val logger = KotlinLogging.logger {}
    }
}
