package org.vitrivr.engine.core.model.retrievable.attributes

/**
 * An attribute that holds arbitrary named string properties.
 */
class PropertyAttribute(properties: Map<String, String>) : MergingRetrievableAttribute {

    val properties: Map<String, String> = properties.toMap()
    override fun merge(other: MergingRetrievableAttribute): PropertyAttribute = PropertyAttribute(this.properties + (other as PropertyAttribute).properties)
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PropertyAttribute

        return properties == other.properties
    }

    override fun hashCode(): Int {
        return properties.hashCode()
    }


}