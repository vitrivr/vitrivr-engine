package org.vitrivr.engine.query.model.api.input

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class InputType {
    @SerialName("TEXT")
    TEXT,
    @SerialName("IMAGE")
    IMAGE,
    @SerialName("VECTOR")
    VECTOR,
    @SerialName("ID")
    ID,
    @SerialName("BOOLEAN")
    BOOLEAN,
    @SerialName("NUMERIC")
    NUMERIC,
    @SerialName("DATETIME")
    DATETIME,
    @SerialName("GEOGRAPHY")
    GEOGRAPHY
}
