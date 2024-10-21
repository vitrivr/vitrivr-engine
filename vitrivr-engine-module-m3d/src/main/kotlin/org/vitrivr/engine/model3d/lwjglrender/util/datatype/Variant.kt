package org.vitrivr.engine.model3d.lwjglrender.util.datatype

/**
 * A variant class that can hold any type of object To store an object in the variant use the set method and the generic identifier of the type e.g. variant.<Model>set("name", model); To retrieve an object from the variant use the get method and the generic identifier of the type e.g. var model = variant.<Model>get("name"); The variant class can also be merged with other variants.
</Model></Model> */
class Variant {
    private val variants: MutableMap<String, Any> = HashMap()


    /**
     * Add a value of type T to the variant If T is a variant, all values of the variant are added to the current variant
     *
     * @param key   Key of the value If the key already exists, an exception is thrown
     * @param value Value to add
     */
    fun <T : Any> set(key: String, value: T): Variant {
        try {
            if (value is Variant) {
                variants.putAll((value as Variant).variants)
            } else {
                variants[key] = value
            }
        } catch (ex: IllegalArgumentException) {
            throw VariantException("Key already exists")
        }
        return this
    }

    /**
     * Get the value stored under the given key from the variant.
     * If the key does not exist, an exception is thrown
     * If the type of the value does not match the type of T, an exception is thrown
     */
    @Throws(VariantException::class)
    fun <T> get(clazz: Class<T>, key: String): T {
        val `val` = variants[key]
        val result: T
        try {
            result = clazz.cast(`val`)
        } catch (ex: Exception) {
            throw VariantException("type mismatch" + `val`!!.javaClass, ex)
        }
        return result
    }

    /**
     * Get the value stored under the given key from the variant and remove it from the variant.
     * If the key does not exist, an exception is thrown
     * If the type of the value does not match the type of T, an exception is thrown
     */
    @Throws(VariantException::class)
    fun <T> remove(clazz: Class<T>, key: String): T {
        val `val` = variants.remove(key)
        val result: T
        try {
            result = clazz.cast(`val`)
        } catch (ex: ClassCastException) {
            throw VariantException("type mismatch")
        }
        return result
    }

    /**
     * Clears the variant
     */
    fun clear() {
        variants.clear()
    }
}