package org.vitrivr.engine.core.model.color

/**
 * A container for YCbCr colors.
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
@JvmInline
value class YCbCrColorContainer constructor(private val ycbcr: IntArray) {
    init {
        require(this.ycbcr.size == 3) { "YCbCrColorContainer must have exactly 3 elements." }
    }

    constructor(y: Int, cb: Int, cr: Int) : this(intArrayOf(y, cb, cr))

    /** Accessor for the L component of the [LabColorContainer]. */
    val y: Int
        get() = this.ycbcr[0]

    /** Accessor for the A component of the [LabColorContainer]. */
    val cb: Int
        get() = this.ycbcr[1]

    /** Accessor for the B component of the [LabColorContainer]. */
    val cr: Int
        get() = this.ycbcr[2]
}