package org.vitrivr.engine.query.model.api.operator
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.vitrivr.engine.core.model.query.filter.EarlyFilter

@Serializable
class OperatorDescription(
    val factory: String? = null,
    val field: String? = null,
    @SerialName("name")
    private val inName: String? = field,
    val inputs: Map<String, String>,
    val parameters: Map<String, String> = emptyMap(),
    val filter: EarlyFilter? = null,
) {

    init {
        require(filter == null || field != null || factory?.substringAfterLast('.') == "FilteredRetrieval") { "Early filters must be attached to retrieval operations." }
        filter?.validate()
        if (this.field == null && this.factory == null) {
            throw IllegalArgumentException("field or factory required")
        }
    }
}
