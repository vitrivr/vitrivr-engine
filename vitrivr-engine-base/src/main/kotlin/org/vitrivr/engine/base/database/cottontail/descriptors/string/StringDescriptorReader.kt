package org.vitrivr.engine.base.database.cottontail.descriptors.string

import org.vitrivr.cottontail.client.language.basics.predicate.Compare
import org.vitrivr.cottontail.core.tuple.Tuple
import org.vitrivr.engine.base.database.cottontail.CottontailConnection
import org.vitrivr.engine.base.database.cottontail.CottontailConnection.Companion.DESCRIPTOR_ID_COLUMN_NAME
import org.vitrivr.engine.base.database.cottontail.CottontailConnection.Companion.RETRIEVABLE_ID_COLUMN_NAME
import org.vitrivr.engine.base.database.cottontail.descriptors.AbstractDescriptorReader
import org.vitrivr.engine.base.database.cottontail.descriptors.DESCRIPTOR_COLUMN_NAME
import org.vitrivr.engine.core.model.descriptor.scalar.StringDescriptor
import org.vitrivr.engine.core.model.retrievable.Retrieved
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.query.Query
import org.vitrivr.engine.core.model.query.string.TextQuery
import java.util.*

class StringDescriptorReader(field: Schema.Field<*, StringDescriptor>, connection: CottontailConnection) : AbstractDescriptorReader<StringDescriptor>(field, connection) {
    override fun getAll(query: Query<StringDescriptor>): Sequence<Retrieved> = when (query) {
        is TextQuery -> {
            val cottontailQuery = org.vitrivr.cottontail.client.language.dql.Query(this.entityName)
                    .select(RETRIEVABLE_ID_COLUMN_NAME).where(
                            Compare(
                                    org.vitrivr.cottontail.client.language.basics.expression.Column(this.entityName.column(DESCRIPTOR_COLUMN_NAME)),
                                    Compare.Operator.LIKE, // TODO: This is not correct, in future we need to use a full-text search.
                                    org.vitrivr.cottontail.client.language.basics.expression.Literal(query.descriptor.value)
                            )
                    )
            this.connection.client.query(cottontailQuery).asSequence().mapNotNull {
                val retrievableId = it.asString(RETRIEVABLE_ID_COLUMN_NAME)
                        ?: return@mapNotNull null
                Retrieved.Default(id = UUID.fromString(retrievableId), type = null, transient = false)
            }
        }
        else -> throw UnsupportedOperationException("Query of typ ${query::class} is not supported by FloatVectorDescriptorReader.")
    }

    override fun tupleToDescriptor(tuple: Tuple): StringDescriptor {
        val descriptorId = UUID.fromString(
                tuple.asString(CottontailConnection.DESCRIPTOR_ID_COLUMN_NAME)
                        ?: throw IllegalArgumentException("The provided tuple is missing the required field '${DESCRIPTOR_ID_COLUMN_NAME}'.")
        )
        val retrievableId = UUID.fromString(
                tuple.asString(RETRIEVABLE_ID_COLUMN_NAME)
                        ?: throw IllegalArgumentException("The provided tuple is missing the required field '${RETRIEVABLE_ID_COLUMN_NAME}'.")
        )
        return StringDescriptor(
                descriptorId,
                retrievableId,
                tuple.asString(DESCRIPTOR_COLUMN_NAME)
                        ?: throw IllegalArgumentException("The provided tuple is missing the required field '$DESCRIPTOR_COLUMN_NAME'.")
        )
    }
}