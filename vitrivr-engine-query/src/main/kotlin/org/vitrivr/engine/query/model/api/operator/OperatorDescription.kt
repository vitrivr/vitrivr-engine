package org.vitrivr.engine.query.model.api.operator
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class OperatorDescription(
    val factory: String? = null,
    val field: String? = null,
    @SerialName("name")
    private val inName: String? = field,
    val inputs: Map<String, String>,
    val parameters: Map<String, String> = emptyMap(),
) {

    init {
        if (this.field == null && this.factory == null) {
            throw IllegalArgumentException("field or factory required")
        }
    }
}
