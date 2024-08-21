package org.vitrivr.engine.core.model.retrievable.attributes

import org.vitrivr.engine.core.model.descriptor.Descriptor
import org.vitrivr.engine.core.model.descriptor.DescriptorId

class DescriptorAuthorAttribute() : MergingRetrievableAttribute {

    private val idToAuthorMap = HashMap<DescriptorId, String>()
    private val authorToIdMap = HashMap<String, MutableSet<DescriptorId>>()

    constructor(id: DescriptorId, author: String): this() {
        add(id, author)
    }

    constructor(pairs: Collection<Pair<DescriptorId, String>>): this() {
        require(pairs.isNotEmpty()) { "At least one pair must be defined" }
        pairs.forEach {
            add(it.first, it.second)
        }
    }

    @Synchronized
    fun add(id: DescriptorId, author: String): DescriptorAuthorAttribute {
        idToAuthorMap[id] = author
        authorToIdMap.computeIfAbsent(author) { HashSet() }.add(id)
        return this
    }

    fun add(descriptor: Descriptor<*>, author: String) = add(descriptor.id, author)

    @Synchronized
    fun getAuthor(id: DescriptorId): String? = idToAuthorMap[id]

    @Synchronized
    fun getAuthors(): Set<String> = authorToIdMap.keys.toSet()

    @Synchronized
    fun getDescriptorIds(author: String): Set<DescriptorId> = authorToIdMap[author] ?: emptySet()

    @Synchronized
    fun getDescriptorIds(): Set<DescriptorId> = idToAuthorMap.keys.toSet()

    override fun merge(other: MergingRetrievableAttribute): MergingRetrievableAttribute {

        require(other is DescriptorAuthorAttribute)

        val (smaller, larger) = if (this.idToAuthorMap.size < other.idToAuthorMap.size) Pair(this, other) else Pair(this, other)

        smaller.idToAuthorMap.forEach {
            larger.add(it.key, it.value)
        }

        return larger
    }

}