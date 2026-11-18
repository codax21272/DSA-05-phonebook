package oy.tol.tra;

public class KeyValueBSearchTree<K extends Comparable<K>, V> implements Dictionary<K, V> {
    private TreeNode<K, V> root;
    private int count;
    private int maxTreeDepth;

    @Override public Type getType() { return Type.BST; }
    @Override public int size() { return count; }

    @Override public boolean add(K key, V value) {
        if (key == null || value == null) throw new IllegalArgumentException("Key and value cannot be null");
        if (root == null) {
            root = new TreeNode<>(key, value);
            count = 1;
        } else {
            TreeNode.currentAddTreeDepth = 0;
            count += root.insert(key, value, key.hashCode());
            maxTreeDepth = Math.max(maxTreeDepth, TreeNode.currentAddTreeDepth);
        }
        return true;
    }

    @Override public V find(K key) {
        if (key == null) throw new IllegalArgumentException("Key cannot be null");
        return root == null ? null : root.find(key, key.hashCode());
    }

    @Override public void ensureCapacity(int capacity) { }
    @Override public void compress() { }

    @Override public Pair<K, V>[] toSortedArray() {
        TreeToArrayVisitor<K, V> visitor = new TreeToArrayVisitor<>(count);
        if (root != null) root.accept(visitor);
        Pair<K, V>[] sorted = visitor.getArray();
        Algorithms.fastSort(sorted);
        return sorted;
    }

    @Override public String getStatus() {
        TreeAnalyzerVisitor<K, V> visitor = new TreeAnalyzerVisitor<>();
        if (root != null) root.accept(visitor);
        return "Tree has max depth of " + maxTreeDepth + ".\n"
                + "Longest collision chain in this tree is " + visitor.longestCollisionChain + "\n"
                + "Min path height to bottom: " + (root == null ? 0 : visitor.minHeight) + "\n"
                + "Max path height to bottom: " + (root == null ? 0 : visitor.maxHeight) + "\n"
                + "Ideal height if balanced: " + (count == 0 ? 0 : (int) (Math.log(count) / Math.log(2))) + "\n";
    }
}
