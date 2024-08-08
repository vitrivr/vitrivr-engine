package org.vitrivr.engine.core.util.knn

import java.util.*

/**
 * Ordered List of fixed size, used for KNN operations
 */

class FixedSizePriorityQueue<T>(private val maxSize: Int, comparator: Comparator<T>) : TreeSet<T>(comparator) {

    init {
        require(maxSize > 0) { "Maximum size must be greater than zero." }
    }

    private val elementsLeft: Int
        get() = this.maxSize - this.size

    override fun add(element: T): Boolean {
        if (elementsLeft > 0) {
            // queue isn't full => add element and decrement elementsLeft
            val added = super.add(element)
            return added
        } else {
            // there is already 1 or more elements => compare to the least
            val compared = super.comparator().compare(this.last(), element)
            if (compared > 0) {
                // new element is larger than the least in queue => pull the least and add new one to queue
                pollLast()
                super.add(element)
                return true
            } else {
                // new element is less than the least in queue => return false
                return false
            }
        }
    }

}