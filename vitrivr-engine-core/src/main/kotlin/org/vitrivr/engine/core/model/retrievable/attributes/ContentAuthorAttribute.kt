package org.vitrivr.engine.core.model.retrievable.attributes

import java.util.*
import kotlin.collections.HashMap

class ContentAuthorAttribute private constructor(
    private val authorMap: HashMap<UUID, HashSet<String>>
) : MergingRetrievableAttribute {

    constructor(contentId: UUID, author: String) : this(hashMapOf(contentId to hashSetOf(author)))

    override fun merge(other: MergingRetrievableAttribute): MergingRetrievableAttribute {
        val otherMap = (other as ContentAuthorAttribute).authorMap
        for ((contentId, authors) in otherMap) {
            authorMap.computeIfAbsent(contentId) { hashSetOf() }.addAll(authors)
        }
        return ContentAuthorAttribute(authorMap)
    }

    fun getAuthors(contentId: UUID): Set<String> {
        return authorMap[contentId] ?: emptySet()
    }
}