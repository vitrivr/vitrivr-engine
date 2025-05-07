package org.vitrivr.engine.query.model.api.operator
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class OperatorDescription(
    val className: String,
    val field: String? = null,
    @SerialName("name")
    private val inName: String? = field,
    val inputs: Map<String, String>,
    val parameters: Map<String, String>
) {

    init {
        if (this.field == null && this.inName == null) {
            throw IllegalArgumentException("field or name required")
        }
    }

    val name: String
        get() = this.field ?: this.inName!!
}
