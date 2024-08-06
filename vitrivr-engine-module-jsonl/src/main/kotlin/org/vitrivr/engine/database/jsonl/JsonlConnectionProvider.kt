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
import org.vitrivr.engine.database.jsonl.scalar.ScalarJsonlProvider
import org.vitrivr.engine.database.jsonl.struct.StructJsonlProvider
import org.vitrivr.engine.database.jsonl.vector.VectorJsonlProvider
import java.nio.file.Path


class JsonlConnectionProvider() : AbstractConnectionProvider() {

    override val databaseName = "JSONL File Connection"
    override val version = "1.0.0"

    override fun openConnection(schemaName: String, parameters: Map<String, String>): Connection {

        val rootPath = parameters["root"] ?: "."

        return JsonlConnection(schemaName, this, Path.of(rootPath))

    }

    override fun initialize() {
        /* Scalar descriptors. */
        this.register(BooleanDescriptor::class, ScalarJsonlProvider)
        this.register(IntDescriptor::class, ScalarJsonlProvider)
        this.register(LongDescriptor::class, ScalarJsonlProvider)
        this.register(FloatDescriptor::class, ScalarJsonlProvider)
        this.register(DoubleDescriptor::class, ScalarJsonlProvider)
        this.register(StringDescriptor::class, ScalarJsonlProvider)

        /* Vector descriptors. */
        this.register(BooleanVectorDescriptor::class, VectorJsonlProvider)
        this.register(IntVectorDescriptor::class, VectorJsonlProvider)
        this.register(LongVectorDescriptor::class, VectorJsonlProvider)
        this.register(FloatVectorDescriptor::class, VectorJsonlProvider)
        this.register(DoubleVectorDescriptor::class, VectorJsonlProvider)

        /* Struct descriptor. */
        this.register(LabelDescriptor::class, StructJsonlProvider)
        this.register(FileSourceMetadataDescriptor::class, StructJsonlProvider)
        this.register(VideoSourceMetadataDescriptor::class, StructJsonlProvider)
        this.register(TemporalMetadataDescriptor::class, StructJsonlProvider)
        this.register(Rectangle2DMetadataDescriptor::class, StructJsonlProvider)
        this.register(MediaDimensionsDescriptor::class, StructJsonlProvider)
        this.register(MapStructDescriptor::class, StructJsonlProvider)
    }

}