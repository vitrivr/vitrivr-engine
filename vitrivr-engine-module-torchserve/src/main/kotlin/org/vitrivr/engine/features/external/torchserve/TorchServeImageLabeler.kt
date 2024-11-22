package org.vitrivr.engine.features.external.torchserve

import com.google.protobuf.ByteString
import kotlinx.serialization.json.Json
import org.vitrivr.engine.core.context.QueryContext
import org.vitrivr.engine.core.model.content.element.ImageContent
import org.vitrivr.engine.core.model.descriptor.struct.LabelDescriptor
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.query.Query
import org.vitrivr.engine.core.operators.retrieve.Retriever
import org.vitrivr.engine.features.external.torchserve.basic.TorchServe
import java.io.ByteArrayOutputStream
import java.util.*
import javax.imageio.ImageIO
import kotlin.reflect.KClass

/**
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
class TorchServeImageLabeler : TorchServe<ImageContent, LabelDescriptor>() {

    override val contentClasses = setOf(ImageContent::class)
    override val descriptorClass: KClass<LabelDescriptor> = LabelDescriptor::class

    override fun prototype(field: Schema.Field<*, *>) = LabelDescriptor(UUID.randomUUID(), UUID.randomUUID(), "", 0.0f, null)

    /**
     * Converts the [ImageContent] to a [ByteString].
     *
     * @param content [ImageContent] to convert.
     * @return Map containing the [ByteString] representation of the [ImageContent].
     */
    override fun toByteString(content: ImageContent): ByteString {
        val output = ByteArrayOutputStream()
        ImageIO.write(content.content, "JPEG", output)
        return ByteString.copyFrom(output.toByteArray())
    }

    /**
     * Converts a [ByteString] to a [List] of [LabelDescriptor]s.
     *
     * @param byteString [ByteString] to convert.
     * @return Map containing the [ByteString] representation of the [ImageContent].
     */
    override fun byteStringToDescriptor(byteString: ByteString): List<LabelDescriptor> {
        val decoded = Json.decodeFromString<Map<String, Float>>(byteString.toString(Charsets.UTF_8))
        return decoded.map { (label, confidence) -> LabelDescriptor(UUID.randomUUID(), UUID.randomUUID(), label, confidence, null) }
    }


    /**
     *
     */
    override fun newRetrieverForQuery(
        field: Schema.Field<ImageContent, LabelDescriptor>,
        query: Query,
        context: QueryContext
    ): Retriever<ImageContent, LabelDescriptor> {
        TODO("Not yet implemented")
    }
}