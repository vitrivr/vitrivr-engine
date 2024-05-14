package org.vitrivr.engine.core.util.tree

/**
 * Simple tree node representation, with a name.
 * A [TreeNode] is considered to be a _leaf_, if there aren't any children defined.
 */
data class TreeNode<T>(
    /**
     * The name of the node.
     */
    val name: String,

    /**
     * The value of this [TreeNode].
     */
    val value: T,

    /**
     * The children of this [TreeNode]. If this is empty, this [TreeNode] is a leaf.
     */
    val children: MutableList<TreeNode<T>> = mutableListOf()
) {


    /**
     * Returns whether this [TreeNode] is a leaf, therefore it has no [children].
     *
     * @return TRUE if and only if there are no [children]. FALSE otherwise
     */
    fun isLeaf(): Boolean {
        return children.isEmpty()
    }

}
