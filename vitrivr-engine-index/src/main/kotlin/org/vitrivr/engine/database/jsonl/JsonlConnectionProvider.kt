package org.vitrivr.engine.database.jsonl

import org.vitrivr.engine.core.database.AbstractConnectionProvider
import org.vitrivr.engine.core.database.Connection
import org.vitrivr.engine.core.model.descriptor.scalar.*
import org.vitrivr.engine.core.model.descriptor.struct.LabelDescriptor
import org.vitrivr.engine.core.model.descriptor.struct.MapStructDescriptor
import org.vitrivr.engine.core.model.descriptor.struct.metadata.MediaDimensionsDescriptor
import org.vitrivr.engine.core.model.descriptor.struct.metadata.Rectangle2DMetadataDescriptor
import org.vitrivr.engine.core.model.descriptor.struct.metadata.TemporalMetadataDescriptor
import org.vitrivr.engine.core.model.descriptor.struct.metadata.source.FileSourceMetadataDescriptor
import org.vitrivr.engine.core.model.descriptor.struct.metadata.source.VideoSourceMetadataDescriptor
import org.vitrivr.engine.core.model.descriptor.vector.*
import java.io.File


class JsonlConnectionProvider() : AbstractConnectionProvider() {

    override val databaseName = "JSONL File Connection"
    override val version = "1.0.0"

    override fun openConnection(schemaName: String, parameters: Map<String, String>): Connection {

        val rootPath = parameters["root"] ?: "."

        return JsonlConnection(schemaName, this, File(rootPath))

    }

    override fun initialize() {
        /* Scalar descriptors. */
        this.register(BooleanDescriptor::class, JsonlProvider<BooleanDescriptor>())
        this.register(IntDescriptor::class, JsonlProvider<IntDescriptor>())
        this.register(LongDescriptor::class, JsonlProvider<LongDescriptor>())
        this.register(FloatDescriptor::class, JsonlProvider<FloatDescriptor>())
        this.register(DoubleDescriptor::class, JsonlProvider<DoubleDescriptor>())
        this.register(StringDescriptor::class, JsonlProvider<StringDescriptor>())

        /* Vector descriptors. */
        this.register(BooleanVectorDescriptor::class, JsonlProvider<BooleanVectorDescriptor>())
        this.register(IntVectorDescriptor::class, JsonlProvider<IntVectorDescriptor>())
        this.register(LongVectorDescriptor::class, JsonlProvider<LongVectorDescriptor>())
        this.register(FloatVectorDescriptor::class, JsonlProvider<FloatVectorDescriptor>())
        this.register(DoubleVectorDescriptor::class, JsonlProvider<DoubleVectorDescriptor>())

        /* Struct descriptor. */
        this.register(LabelDescriptor::class, JsonlProvider<LabelDescriptor>())
        this.register(FileSourceMetadataDescriptor::class, JsonlProvider<FileSourceMetadataDescriptor>())
        this.register(VideoSourceMetadataDescriptor::class, JsonlProvider<VideoSourceMetadataDescriptor>())
        this.register(TemporalMetadataDescriptor::class, JsonlProvider<TemporalMetadataDescriptor>())
        this.register(Rectangle2DMetadataDescriptor::class, JsonlProvider<Rectangle2DMetadataDescriptor>())
        this.register(MediaDimensionsDescriptor::class, JsonlProvider<MediaDimensionsDescriptor>())
        this.register(MapStructDescriptor::class, JsonlProvider<MapStructDescriptor>())
    }

}