package org.vitrivr.engine.query.model.api.result

import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.json.JsonContentPolymorphicSerializer
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.vitrivr.engine.core.model.types.Type

import org.vitrivr.engine.query.model.api.input.BooleanInputData
import org.vitrivr.engine.query.model.api.input.BooleanVectorInputData
import org.vitrivr.engine.query.model.api.input.ByteInputData
import org.vitrivr.engine.query.model.api.input.DateInputData
import org.vitrivr.engine.query.model.api.input.DoubleInputData
import org.vitrivr.engine.query.model.api.input.DoubleVectorInputData
import org.vitrivr.engine.query.model.api.input.FloatInputData
import org.vitrivr.engine.query.model.api.input.FloatVectorInputData
import org.vitrivr.engine.query.model.api.input.ImageInputData
import org.vitrivr.engine.query.model.api.input.InputData
import org.vitrivr.engine.query.model.api.input.InputType
import org.vitrivr.engine.query.model.api.input.IntInputData
import org.vitrivr.engine.query.model.api.input.IntVectorInputData
import org.vitrivr.engine.query.model.api.input.LongInputData
import org.vitrivr.engine.query.model.api.input.LongVectorInputData
import org.vitrivr.engine.query.model.api.input.RetrievableIdInputData
import org.vitrivr.engine.query.model.api.input.ShortInputData
import org.vitrivr.engine.query.model.api.input.StringInputData
import org.vitrivr.engine.query.model.api.input.TextInputData

