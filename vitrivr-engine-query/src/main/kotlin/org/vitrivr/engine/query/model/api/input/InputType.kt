package org.vitrivr.engine.query.model.api.input

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class InputType(val isDescriptor: Boolean , val isContent: Boolean) {
    // Content types
    @SerialName("IMAGE")
    IMAGE(isDescriptor = false, isContent = true),

    @SerialName("DATE")
    DATE(isDescriptor = true, isContent = false),



    @SerialName("ID")
    ID(isDescriptor = false, isContent = true),

    // Scalar types
    @SerialName("BOOLEAN")
    BOOLEAN(isDescriptor = true, isContent = false),

    @SerialName("BYTE")
    BYTE(isDescriptor = true, isContent = false),

    @SerialName("DOUBLE")
    DOUBLE(isDescriptor = true, isContent = false),

    @SerialName("FLOAT")
    FLOAT(isDescriptor = true, isContent = false),

    @SerialName("INT")
    INT(isDescriptor = true, isContent = false),

    @SerialName("LONG")
    LONG(isDescriptor = true, isContent = false),

    @SerialName("SHORT")
    SHORT(isDescriptor = true, isContent = false),

    @SerialName("STRING")
    STRING(isDescriptor = false, isContent = true),

    @SerialName("TEXT")
    TEXT(isDescriptor = false, isContent = true),

    // Vector types
    @SerialName("BOOLEANVECTOR")
    BOOLEANVECTOR(isDescriptor = true, isContent = false),

    @SerialName("DOUBLEVECTOR")
    DOUBLEVECTOR(isDescriptor = true, isContent = false),

    @SerialName("FLOATVECTOR")
    FLOATVECTOR(isDescriptor = true, isContent = false),

    @SerialName("INTVECTOR")
    INTVECTOR(isDescriptor = true, isContent = false),

    @SerialName("LONGVECTOR")
    LONGVECTOR(isDescriptor = true, isContent = false),
}

