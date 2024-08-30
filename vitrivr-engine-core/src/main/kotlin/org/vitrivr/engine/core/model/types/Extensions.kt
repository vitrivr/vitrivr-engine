package org.vitrivr.engine.core.model.types

fun String.toValue(): Value.String = Value.String(this)
fun Boolean.toValue() = Value.Boolean(this)
fun Byte.toValue() = Value.Byte(this)
fun Short.toValue() = Value.Short(this)
fun Int.toValue() = Value.Int(this)
fun Long.toValue() = Value.Long(this)
fun Float.toValue() = Value.Float(this)
fun Double.toValue() = Value.Double(this)
