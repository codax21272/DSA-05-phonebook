import oy.tol.tra.*;

/** Additional edge cases; leaves the instructor's tests and fixtures untouched. */
public class DictionaryChecks {
    private static class Key implements Comparable<Key> {
        final int id;
        final int hash;
        Key(int id, int hash) { this.id = id; this.hash = hash; }
        public int hashCode() { return hash; }
        public boolean equals(Object other) { return other instanceof Key && id == ((Key) other).id; }
        public int compareTo(Key other) { return Integer.compare(id, other.id); }
    }

    public static void main(String[] args) {
        check(new KeyValueHashTable<>());
        check(new KeyValueBSearchTree<>());
        // These names have identical full-name identity despite different field boundaries.
        Person first = new Person("B C", "A");
        Person second = new Person("C", "A B");
        require(first.equals(second) && first.compareTo(second) == 0
                && first.hashCode() == second.hashCode(), "Person identity contract");
        System.out.println("Additional dictionary edge checks passed for HASHTABLE and BST.");
    }

    private static void check(Dictionary<Key, Integer> dictionary) {
        require(dictionary.size() == 0 && dictionary.toSortedArray().length == 0, "Empty dictionary");
        dictionary.getStatus();
        dictionary.compress();
        require(dictionary.find(new Key(-1, 0)) == null, "Empty search");
        for (int i = 0; i < 400; i++) {
            // Force collisions, negative values, MIN_VALUE and probe wraparound.
            int hash = i % 3 == 0 ? Integer.MIN_VALUE : (i % 3 == 1 ? -1 : Integer.MAX_VALUE);
            dictionary.add(new Key(i, hash), i);
        }
        for (int i = 0; i < 400; i++) {
            int hash = i % 3 == 0 ? Integer.MIN_VALUE : (i % 3 == 1 ? -1 : Integer.MAX_VALUE);
            dictionary.add(new Key(i, hash), i + 1000);
            require(dictionary.find(new Key(i, hash)) == i + 1000, "Collision-chain replacement");
        }
        require(dictionary.size() == 400, "Duplicate count");
        dictionary.compress();
        Pair<Key, Integer>[] sorted = dictionary.toSortedArray();
        require(sorted.length == 400, "Sorted count");
        for (int i = 0; i < sorted.length; i++) {
            require(sorted[i].getKey().id == i && sorted[i].getValue() == i + 1000, "Sorted contents");
        }
        Key firstKey = sorted[0].getKey();
        sorted[0].setValue(-1);
        require(dictionary.find(firstKey) == 1000, "Export independence");
        illegal(() -> dictionary.add(null, 1));
        illegal(() -> dictionary.add(firstKey, null));
        illegal(() -> dictionary.find(null));
        require(dictionary.find(new Key(999, -1)) == null, "Absent colliding key");
        if (dictionary.getType() == Dictionary.Type.HASHTABLE) {
            try {
                dictionary.ensureCapacity(Integer.MAX_VALUE);
                throw new AssertionError("Impossible capacity must fail");
            } catch (OutOfMemoryError expected) { }
            require(dictionary.size() == 400 && dictionary.find(firstKey) == 1000, "Failed allocation preservation");
            dictionary.ensureCapacity(20);
            require(dictionary.size() == 0 && dictionary.find(firstKey) == null, "Capacity reset contract");
        }
    }

    private static void illegal(Runnable operation) {
        try { operation.run(); } catch (IllegalArgumentException expected) { return; }
        throw new AssertionError("Null argument must fail");
    }
    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
