package org.vitrivr.engine.query.model.api.input

import io.javalin.openapi.Discriminator
import io.javalin.openapi.DiscriminatorProperty
import io.javalin.openapi.OneOf
import kotlinx.serialization.Serializable
import org.vitrivr.engine.core.model.content.element.ContentElement
import org.vitrivr.engine.core.model.content.element.DescriptorContent
import org.vitrivr.engine.core.model.content.element.IdContent
import org.vitrivr.engine.core.model.content.element.ImageContent
import org.vitrivr.engine.core.model.content.element.TextContent
import org.vitrivr.engine.core.model.content.impl.memory.InMemoryImageContent
import org.vitrivr.engine.core.model.content.impl.memory.InMemoryTextContent
import org.vitrivr.engine.core.model.descriptor.Descriptor
import org.vitrivr.engine.core.model.descriptor.scalar.BooleanDescriptor
import org.vitrivr.engine.core.model.descriptor.scalar.ByteDescriptor
import org.vitrivr.engine.core.model.descriptor.scalar.DoubleDescriptor
import org.vitrivr.engine.core.model.descriptor.scalar.FloatDescriptor
import org.vitrivr.engine.core.model.descriptor.scalar.IntDescriptor
import org.vitrivr.engine.core.model.descriptor.scalar.LongDescriptor
import org.vitrivr.engine.core.model.descriptor.scalar.ShortDescriptor
import org.vitrivr.engine.core.model.descriptor.scalar.StringDescriptor
import org.vitrivr.engine.core.model.descriptor.scalar.TextDescriptor
import org.vitrivr.engine.core.model.descriptor.vector.BooleanVectorDescriptor
import org.vitrivr.engine.core.model.descriptor.vector.DoubleVectorDescriptor
import org.vitrivr.engine.core.model.descriptor.vector.FloatVectorDescriptor
import org.vitrivr.engine.core.model.descriptor.vector.IntVectorDescriptor
import org.vitrivr.engine.core.model.descriptor.vector.LongVectorDescriptor
import org.vitrivr.engine.core.model.types.Value
import org.vitrivr.engine.core.model.types.toValue
import org.vitrivr.engine.core.util.extension.BufferedImage
import java.text.SimpleDateFormat
import java.util.*
import kotlin.Double

/**
 * The abstract [InputData], essentially a query's input value.
 */
@Serializable(with = InputDataSerializer::class)
@OneOf(
    discriminator = Discriminator(DiscriminatorProperty("type", type = InputType::class)),
    value = [
        TextInputData::class,
        ImageInputData::class,
        FloatVectorInputData::class,
        RetrievableIdInputData::class,
        BooleanInputData::class,
        ByteInputData::class,
        DoubleInputData::class,
        FloatInputData::class,
        IntInputData::class,
        LongInputData::class,
        ShortInputData::class,
        StringInputData::class,
        BooleanVectorInputData::class,
        DoubleVectorInputData::class,
        IntVectorInputData::class,
        LongVectorInputData::class,
        DateInputData::class
    ]
)
sealed interface InputData {
    /**
     * The [InputType] of this [InputType]. Required for polymorphic deserialisation.
     */
    val type: InputType

    /**
     * Converts the given [InputData] to a [ContentElement] if supported.
     *
     * @throws UnsupportedOperationException If there is no way to convert the input to a content
     */
    fun toContent(): ContentElement<*>
    fun toDescriptor(): Descriptor<*>
}

/**
 * [InputData] for textual input.
 * Can be converted to a [ContentElement], specifically a [TextContent].
 */
@Serializable
data class TextInputData(val data: String) : InputData {
    override val type = InputType.TEXT
    override fun toContent(): DescriptorContent<TextDescriptor> = InMemoryTextContent(data)
    override fun toDescriptor(): TextDescriptor =
        throw UnsupportedOperationException("TextInputData cannot be converted to a descriptor")
}


/**
 * [InputData] for image input in base64 format.
 * Can be converted to a [ContentElement], specifically to a [InMemoryImageContent].
 */
@Serializable
data class ImageInputData(val data: String) : InputData {
    override val type = InputType.IMAGE
    override fun toContent(): ImageContent = InMemoryImageContent(BufferedImage(data))
    override fun toDescriptor(): Descriptor<*> =
        throw UnsupportedOperationException("ImageInputData cannot be converted to a descriptor")
}

/**
 * [InputData] for a retrievable id.
 * Cannot be converted to a [ContentElement]
 */
@Serializable
data class RetrievableIdInputData(val id: String) : InputData {
    override val type = InputType.ID
    override fun toContent(): IdContent = IdContent(UUID.fromString(id))
    override fun toDescriptor(): Descriptor<*> =
        throw UnsupportedOperationException("RetrievableIdInputData cannot be converted to a descriptor")
}

/**
 * [InputData] for boolean input.
 * Cannot be converted to a [ContentElement]
 */
@Serializable
data class BooleanInputData(val data: Boolean) : InputData {
    override val type = InputType.BOOLEAN
    override fun toContent(): DescriptorContent<BooleanDescriptor> =
        throw UnsupportedOperationException("BooleanInputData cannot be converted to a content element")

    override fun toDescriptor(): BooleanDescriptor =
        BooleanDescriptor(value = Value.Boolean(data))
}

/**
 * [InputData] for byte input.
 * Cannot be converted to a [ContentElement]
 */
@Serializable
data class ByteInputData(val data: Byte) : InputData {
    override val type = InputType.BYTE
    override fun toContent(): DescriptorContent<ByteDescriptor> =
        throw UnsupportedOperationException("ByteInputData cannot be converted to a content element")

    override fun toDescriptor(): ByteDescriptor =
        ByteDescriptor(value = Value.Byte(data))
}

/**
 * [InputData] for Double input.
 * Cannot be converted to a [ContentElement]
 */
@Serializable
data class DoubleInputData(val data: Double) : InputData {
    override val type = InputType.DOUBLE
    override fun toContent(): DescriptorContent<DoubleDescriptor> =
        throw UnsupportedOperationException("DoubleInputData cannot be converted to a content element")

    override fun toDescriptor(): DoubleDescriptor =
        DoubleDescriptor(value = Value.Double(data))
}

/**
 * [InputData] for Float input.
 * Cannot be converted to a [ContentElement]
 */
@Serializable
data class FloatInputData(val data: Float) : InputData {
    override val type = InputType.FLOAT
    override fun toContent(): DescriptorContent<FloatDescriptor> =
        throw UnsupportedOperationException("FloatInputData cannot be converted to a content element")

    override fun toDescriptor(): FloatDescriptor =
        FloatDescriptor(value = Value.Float(data))
}

/**
 * [InputData] for Int input.
 * Cannot be converted to a [ContentElement]
 */
@Serializable
data class IntInputData(val data: Int) : InputData {
    override val type = InputType.INT
    override fun toContent(): DescriptorContent<IntDescriptor> =
        throw UnsupportedOperationException("IntInputData cannot be converted to a content element")

    override fun toDescriptor(): IntDescriptor =
        IntDescriptor(value = Value.Int(data))
}

/**
 * [InputData] for Long input.
 * Cannot be converted to a [ContentElement]
 */
@Serializable
data class LongInputData(val data: Long) : InputData {
    override val type = InputType.LONG
    override fun toContent(): DescriptorContent<LongDescriptor> =
        throw UnsupportedOperationException("LongInputData cannot be converted to a content element")

    override fun toDescriptor(): LongDescriptor =
        LongDescriptor(value = Value.Long(data))
}


/**
 * [InputData] for Short input.
 * Cannot be converted to a [ContentElement]
 */
@Serializable
data class ShortInputData(val data: Short) : InputData {
    override val type = InputType.SHORT
    override fun toContent(): DescriptorContent<ShortDescriptor> =
        throw UnsupportedOperationException("ShortInputData cannot be converted to a content element")

    override fun toDescriptor(): ShortDescriptor =
        ShortDescriptor(value = Value.Short(data))
}

/**
 * [InputData] for String input.
 * Cannot be converted to a [ContentElement]
 */
@Serializable
data class StringInputData(val data: String) : InputData {
    override val type = InputType.STRING
    override fun toContent(): DescriptorContent<StringDescriptor> =
        throw UnsupportedOperationException("StringInputData cannot be converted to a content element")

    override fun toDescriptor(): StringDescriptor =
        StringDescriptor(value = Value.String(data))
}


/**
 * [InputData] for Boolean vector input.
 * Cannot be converted to a [ContentElement]
 */
@Serializable
data class BooleanVectorInputData(val data: List<Boolean>) : InputData {
    override val type = InputType.BOOLEANVECTOR
    override fun toContent(): DescriptorContent<BooleanVectorDescriptor> =
        throw UnsupportedOperationException("BooleanVectorInputData cannot be converted to a content element")

    override fun toDescriptor(): BooleanVectorDescriptor =
        BooleanVectorDescriptor(vector = Value.BooleanVector(data.toBooleanArray()))
}

/**
 * [InputData] for double vector input.
 * Cannot be converted to a [ContentElement]
 */
@Serializable
data class DoubleVectorInputData(val data: List<Double>) : InputData {
    override val type = InputType.DOUBLEVECTOR
    override fun toContent(): DescriptorContent<DoubleVectorDescriptor> =
        throw UnsupportedOperationException("DoubleVectorInputData cannot be converted to a content element")

    override fun toDescriptor(): DoubleVectorDescriptor =
        DoubleVectorDescriptor(vector = Value.DoubleVector(value = data.toDoubleArray()))
}


/**
 * [InputData] for float vector input.
 * Cannot be converted to a [ContentElement]
 */
@Serializable
data class FloatVectorInputData(val data: List<Float>) : InputData {
    override val type = InputType.FLOATVECTOR
    override fun toContent(): DescriptorContent<FloatVectorDescriptor> =
        throw UnsupportedOperationException("VectorInputData cannot be converted to a content element")

    override fun toDescriptor(): FloatVectorDescriptor =
        FloatVectorDescriptor(vector = Value.FloatVector(data.toFloatArray()))
}

/**
 * [InputData] for int vector input.
 * Cannot be converted to a [ContentElement]
 */
@Serializable
data class IntVectorInputData(val data: List<Int>) : InputData {
    override val type = InputType.FLOATVECTOR
    override fun toContent(): DescriptorContent<IntVectorDescriptor> =
        throw UnsupportedOperationException("IntVectorInputData cannot be converted to a content element")

    override fun toDescriptor(): IntVectorDescriptor =
        IntVectorDescriptor(vector = Value.IntVector(data.toIntArray()))
}

/**
 * [InputData] for long vector input.
 * Cannot be converted to a [ContentElement]
 */
@Serializable
data class LongVectorInputData(val data: List<Long>) : InputData {
    override val type = InputType.FLOATVECTOR
    override fun toContent(): DescriptorContent<LongVectorDescriptor> =
        throw UnsupportedOperationException("LongVectorInputData cannot be converted to a content element")

    override fun toDescriptor(): LongVectorDescriptor =
        LongVectorDescriptor(vector = Value.LongVector(data.toLongArray()))
}

/**
 * [InputData] for a date.
 * Cannot be converted to a [ContentElement]
 */
@Serializable
data class DateInputData(val data: String) : InputData {
    override val type = InputType.DATE
    override fun toContent(): ContentElement<*> =
        throw UnsupportedOperationException("DateInputData cannot be converted to a content element")

    override fun toDescriptor(): Descriptor<*> =
        throw UnsupportedOperationException("DateInputData cannot be converted to a descriptor")

    /**
     * Parses the input in YYYY-mm-dd format.
     */
    fun parseDate(): Date {
        val formatter = SimpleDateFormat("YYYY-mm-dd", Locale.ENGLISH)
        return formatter.parse(data)
    }
}

