package org.vitrivr.engine.core.util.tree

/**
 * Simple tree data structure implementation.
 */
class Tree<T>(
) {
    /** The root node's value */
    private lateinit var root: TreeNode<T>

    private val nodesPerName = mutableMapOf<String, TreeNode<T>>()

    private fun addNodeTo(name: String, node: TreeNode<T>) {
        nodesPerName[node.name] = node
        nodesPerName[name]?.children?.add(node) ?: throw IllegalArgumentException("No node with name '$name' found")
    }

    fun add(node: TreeNode<T>) {
        if(this::root.isInitialized) {
            this.root.children.add(node)
        }else{
            this.root = node
        }
        nodesPerName[node.name] = node
    }

    fun addTo(parent: String, node: TreeNode<T>) {
        nodesPerName[parent]?.children?.add(node) ?: throw IllegalArgumentException("No node with name '$parent' found")
        nodesPerName[node.name] = node
    }

    /**
     * Depth-first, pre-order traversal of the tree.
     *
     * @param visitor Processor of the nodes upon traversal. In case of a single-leafed tree, parent is null.
     */
    fun depthFirstPreorder(visitor: (node: TreeNode<T>, parent: TreeNode<T>?) -> Unit){
        depthFirstPreRecursively(root, visitor = visitor)
    }

    fun isEmpty(): Boolean {
        return !this::root.isInitialized
    }

    /**
     * Recursive preorder depth first traversal of the tree.
     */
    private fun depthFirstPreRecursively(node: TreeNode<T>, parent:TreeNode<T>? = null, visitor: (value: TreeNode<T>, parent: TreeNode<T>?) -> Unit ){
        if(node.isLeaf()){
            visitor.invoke(node, parent)
            return
        }
        visitor.invoke(node, parent)
        for(child in node.children) {
            depthFirstPreRecursively(child, node, visitor)
        }
    }
}
