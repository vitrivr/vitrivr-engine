package org.vitrivr.engine.core.model.retrievable.attributes

import org.vitrivr.engine.core.model.content.element.ContentId
import java.util.*
import kotlin.collections.HashMap

const val CONTENT_AUTHORS_KEY = "contentSources"

class ContentAuthorAttribute private constructor(
    private val authorMap: HashMap<ContentId, HashSet<String>>,
    private val contentMap: HashMap<String, HashSet<ContentId>>
) : MergingRetrievableAttribute {

    constructor(contentId: ContentId, author: String) : this(hashMapOf(contentId to hashSetOf(author)), hashMapOf(author to hashSetOf(contentId)))

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

    fun getAuthors(contentId: ContentId): Set<String> {
        return authorMap[contentId] ?: emptySet()
    }

    fun getContentIds(author: String): Set<ContentId> {
        return contentMap[author] ?: emptySet()
    }

    fun getContentIds(authors: Set<String>): Set<ContentId> {
        return authors.flatMap { contentMap[it] ?: emptySet() }.toSet()
    }

}