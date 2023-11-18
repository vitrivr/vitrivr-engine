package org.vitrivr.engine.core.model.retrievable

import java.util.UUID

data class Relationship(
    val sub: Pair<UUID, Retrievable?>,
    val pred: String,
    val obj: Pair<UUID, Retrievable?>
) {
    constructor(sub: Retrievable, pred: String, obj: Retrievable) : this((sub.id to sub), pred, (obj.id to obj))

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Relationship

        if (sub.first != other.sub.first) return false
        if (pred != other.pred) return false
        if (obj.first != other.obj.first) return false

        return true
    }

    override fun hashCode(): Int {
        var result = sub.first.hashCode()
        result = 31 * result + pred.hashCode()
        result = 31 * result + obj.first.hashCode()
        return result
    }

}
