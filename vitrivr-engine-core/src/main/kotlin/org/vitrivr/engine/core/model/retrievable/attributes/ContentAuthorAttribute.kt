package org.vitrivr.engine.core.model.retrievable.attributes

import java.util.*
import kotlin.collections.HashMap

class ContentAuthorAttribute private constructor(
    private val authorMap: HashMap<UUID, HashSet<String>>,
    private val contentMap: HashMap<String, HashSet<UUID>>
) : MergingRetrievableAttribute {

    constructor(contentId: UUID, author: String) : this(hashMapOf(contentId to hashSetOf(author)), hashMapOf(author to hashSetOf(contentId)))

    override fun merge(other: MergingRetrievableAttribute): MergingRetrievableAttribute {
        val otherAuthorMap = (other as ContentAuthorAttribute).authorMap
        for ((contentId, authors) in otherAuthorMap) {
            authorMap.computeIfAbsent(contentId) { hashSetOf() }.addAll(authors)
        }

        val otherContentMap = other.contentMap
        for ((author, contentIds) in otherContentMap) {
            contentMap.computeIfAbsent(author) { hashSetOf() }.addAll(contentIds)
        }

        return ContentAuthorAttribute(authorMap, contentMap)
    }

    fun getAuthors(contentId: UUID): Set<String> {
        return authorMap[contentId] ?: emptySet()
    }

    fun getContentIds(author: String): Set<UUID> {
        return contentMap[author] ?: emptySet()
    }

}