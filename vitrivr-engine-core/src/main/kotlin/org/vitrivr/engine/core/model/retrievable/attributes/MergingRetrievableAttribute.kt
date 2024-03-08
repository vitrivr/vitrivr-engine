package org.vitrivr.engine.core.model.retrievable.attributes

interface MergingRetrievableAttribute : RetrievableAttribute {

    /**
     * Merges two attributes together into a single instance
     */
    fun merge(other: MergingRetrievableAttribute) : MergingRetrievableAttribute

}