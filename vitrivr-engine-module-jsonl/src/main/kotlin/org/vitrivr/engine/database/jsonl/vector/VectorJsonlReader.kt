package org.vitrivr.engine.database.jsonl.vector

import org.vitrivr.engine.core.model.descriptor.vector.*
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.query.Query
import org.vitrivr.engine.core.model.query.basics.SortOrder
import org.vitrivr.engine.core.model.query.proximity.ProximityQuery
import org.vitrivr.engine.core.model.retrievable.Retrieved
import org.vitrivr.engine.core.model.retrievable.attributes.DistanceAttribute
import org.vitrivr.engine.core.model.types.Value
import org.vitrivr.engine.core.util.knn.FixedSizePriorityQueue
import org.vitrivr.engine.database.jsonl.AbstractJsonlReader
import org.vitrivr.engine.database.jsonl.JsonlConnection
import org.vitrivr.engine.database.jsonl.JsonlConnection.Companion.DESCRIPTOR_ID_COLUMN_NAME
import org.vitrivr.engine.database.jsonl.JsonlConnection.Companion.RETRIEVABLE_ID_COLUMN_NAME
import org.vitrivr.engine.database.jsonl.model.AttributeContainerList

class VectorJsonlReader(
    field: Schema.Field<*, VectorDescriptor<*, *>>,
    connection: JsonlConnection
) : AbstractJsonlReader<VectorDescriptor<*, *>>(field, connection) {

    override fun toDescriptor(list: AttributeContainerList): VectorDescriptor<*, *> {

        val map = list.list.associateBy { it.attribute.name }
        val retrievableId = (map[RETRIEVABLE_ID_COLUMN_NAME]?.value!!.toValue() as Value.UUIDValue).value
        val descriptorId = (map[DESCRIPTOR_ID_COLUMN_NAME]?.value!!.toValue() as Value.UUIDValue).value
        val value = map["vector"]?.value!!.toValue()

        return when (prototype) {
            is BooleanVectorDescriptor -> BooleanVectorDescriptor(
                descriptorId,
                retrievableId,
                value as Value.BooleanVector
            )

            is FloatVectorDescriptor -> FloatVectorDescriptor(
                descriptorId,
                retrievableId,
                value as Value.FloatVector
            )

            is DoubleVectorDescriptor -> DoubleVectorDescriptor(
                descriptorId,
                retrievableId,
                value as Value.DoubleVector
            )

            is IntVectorDescriptor -> IntVectorDescriptor(
                descriptorId,
                retrievableId,
                value as Value.IntVector
            )

            is LongVectorDescriptor -> LongVectorDescriptor(
                descriptorId,
                retrievableId,
                value as Value.LongVector
            )
        }
    }

    override fun query(query: Query): Sequence<VectorDescriptor<*, *>> = when (query) {
        is ProximityQuery<*> -> queryProximity(query)
        else -> throw UnsupportedOperationException("Query of typ ${query::class} is not supported by this reader.")
    }

    override fun queryAndJoin(query: Query): Sequence<Retrieved> = when (query) {
        is ProximityQuery<*> -> queryAndJoinProximity(query)
        else -> throw UnsupportedOperationException("Query of typ ${query::class} is not supported by this reader.")
    }


    private fun queryAndJoinProximity(query: ProximityQuery<*>): Sequence<Retrieved> {
        val queue = knn(query)

        val ids = queue.mapNotNull { it.first.retrievableId }

        val retrievables = connection.getRetrievableReader().getAll(ids).associateBy { it.id }

        return queue.map {
            val retrieved = retrievables[it.first.retrievableId]!!
            retrieved.copy(descriptors = retrieved.descriptors + it.first, attributes = retrieved.attributes + DistanceAttribute.Local(it.second, it.first.id))
        }.asSequence()

    }

    private fun queryProximity(query: ProximityQuery<*>): Sequence<VectorDescriptor<*, *>> =
        knn(query).asSequence().map { it.first }


    private fun knn(query: ProximityQuery<*>): FixedSizePriorityQueue<Pair<VectorDescriptor<*, *>, Double>> {

        val queue = FixedSizePriorityQueue(query.k.toInt(),
            when (query.order) {
                SortOrder.ASC -> Comparator<Pair<VectorDescriptor<*, *>, Double>> { p0, p1 ->
                    p0.second.compareTo(p1.second)
                }

                SortOrder.DESC -> Comparator { p0, p1 ->
                    p1.second.compareTo(p0.second)
                }
            }
        )

        getAll().forEach { descriptor ->
            val dist = distance(query, descriptor.vector)
            queue.add(descriptor to dist)
        }

        return queue

    }

    private fun distance(query: ProximityQuery<*>, vector: Value.Vector<*>): Double {
        return when (query.value) {
            is Value.FloatVector -> query.distance(query.value as Value.FloatVector, vector as Value.FloatVector)
            is Value.DoubleVector -> query.distance(
                query.value as Value.DoubleVector,
                vector as Value.DoubleVector
            )

            else -> error("Unsupported query type ${query.value::class.simpleName}")
        }
    }
}