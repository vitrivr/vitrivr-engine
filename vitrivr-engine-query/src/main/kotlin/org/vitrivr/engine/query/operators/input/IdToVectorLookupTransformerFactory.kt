package org.vitrivr.engine.query.operators.input

import org.vitrivr.engine.core.context.Context
import org.vitrivr.engine.core.model.content.element.ContentElement
import org.vitrivr.engine.core.model.descriptor.Descriptor
import org.vitrivr.engine.core.model.descriptor.vector.*
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.query.model.api.input.InputData
import org.vitrivr.engine.query.model.api.input.RetrievableIdInputData
import org.vitrivr.engine.query.model.api.input.VectorInputData
import org.vitrivr.engine.query.model.input.InputDataTransformer
import org.vitrivr.engine.query.model.input.InputDataTransformerFactory
import java.util.*

class IdToVectorLookupTransformerFactory : InputDataTransformerFactory {
    override fun newTransformer(
        name: String,
        inputs: List<InputData>,
        schema: Schema,
        context: Context
    ): InputDataTransformer {

        val idInput = inputs.filterIsInstance<RetrievableIdInputData>().firstOrNull() ?: throw IllegalArgumentException("No RetrievableIdInputData specified in input")
        val fieldName = context[name, "field"] ?: throw IllegalArgumentException("Property 'field' not provided")
        val field = schema[(if(fieldName.contains('.')) fieldName.substringBefore(".") else fieldName)] ?: throw IllegalArgumentException("No field '$fieldName' in schema")

        return IdToVectorLookupTransformer(idInput, field)

    }

    class IdToVectorLookupTransformer(private val input: RetrievableIdInputData, private val field: Schema.Field<ContentElement<*>, Descriptor>) : InputDataTransformer {

        override fun transform(): InputData {
            val id = UUID.fromString(input.id)
            val reader = field.getReader()
            val descriptor = reader.getForRetrievable(id).firstOrNull() ?: throw IllegalArgumentException("No retrievable with id '$id' present in ${field.fieldName}")
            if (descriptor !is VectorDescriptor<*>) {
                throw IllegalArgumentException("retrievable with id '$id' ${field.fieldName} does not contain a vector descriptor")
            }

            return when(descriptor) {
                is FloatVectorDescriptor -> VectorInputData(descriptor.vector.value.toList())
                is BooleanVectorDescriptor -> VectorInputData(descriptor.vector.value.toList().map { if (it) 1f else 0f })
                is DoubleVectorDescriptor -> VectorInputData(descriptor.vector.value.toList().map { it.toFloat() })
                is IntVectorDescriptor -> VectorInputData(descriptor.vector.value.toList().map { it.toFloat() })
                is LongVectorDescriptor -> VectorInputData(descriptor.vector.value.toList().map { it.toFloat() })
            }

        }
    }

}