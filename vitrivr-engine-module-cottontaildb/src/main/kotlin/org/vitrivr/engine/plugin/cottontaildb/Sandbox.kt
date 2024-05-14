package org.vitrivr.engine.plugin.cottontaildb

import io.grpc.ManagedChannel
import io.grpc.ManagedChannelBuilder
import org.vitrivr.cottontail.client.SimpleClient
import org.vitrivr.cottontail.client.language.basics.Direction
import org.vitrivr.cottontail.client.language.basics.Distances
import org.vitrivr.cottontail.client.language.basics.expression.Column
import org.vitrivr.cottontail.client.language.basics.expression.Literal
import org.vitrivr.cottontail.client.language.basics.predicate.Compare
import org.vitrivr.cottontail.client.language.dql.Query
import org.vitrivr.cottontail.core.database.Name
import org.vitrivr.cottontail.core.values.StringValue
import org.vitrivr.cottontail.core.values.UuidValue
import java.util.*

object Sandbox {

    @JvmStatic
    fun main(args: Array<String>) {

        val channel: ManagedChannel = ManagedChannelBuilder.forAddress("127.0.0.1", 1865)
            .enableFullStreamDecompression()
            .usePlaintext()
            .build()

        val client = SimpleClient(channel)


        val query = Query(Name.EntityName.create("mvk", "descriptor_file"))
            .select("*")
            .where(Compare(
                Column(Name.ColumnName.create("mvk", "descriptor_file", "path")),
                Compare.Operator.EQUAL,
                Literal(StringValue("Z:\\Datasets\\MarineVideoKit\\videos-optimized\\Tulamben2_Jun2022_0016.mp4"))),
            ).limit(1000)


        val query1 = Query(Name.EntityName.create("mvk-2", "retrievable"))
            .select("*")
            .where(Compare(
                Column(Name.ColumnName.create("mvk-2", "retrievable", "retrievableid")),
                Compare.Operator.EQUAL,
                Literal(UuidValue(UUID.fromString("8ce4920f-a230-4a6d-8186-20dc029a03af")))),
            ).limit(1000)

        val results = client.query(query1).forEach {
            println(it)
        }
    }

}
