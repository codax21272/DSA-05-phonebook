package oy.tol.tra;

/** Open addressing with bounded linear probing and a maximum load of 0.65. */
public class KeyValueHashTable<K extends Comparable<K>, V> implements Dictionary<K, V> {
    private Pair<K, V>[] values;
    private int count, maxProbingSteps, reallocationCount;
    private long collisionCount;
    private static final double LOAD_FACTOR = 0.65;
    private static final int DEFAULT_CAPACITY = 20;
    private static final int MAX_ARRAY_SIZE = Integer.MAX_VALUE - 8;

    public KeyValueHashTable() { this(DEFAULT_CAPACITY); }
    public KeyValueHashTable(int capacity) { ensureCapacity(capacity); }
    @Override public Type getType() { return Type.HASHTABLE; }
    @Override public int size() { return count; }

    private int capacityFor(int elements) {
        double required = Math.ceil(Math.max(DEFAULT_CAPACITY, elements) / LOAD_FACTOR);
        if (required > MAX_ARRAY_SIZE) throw new OutOfMemoryError("Requested capacity is too large");
        return (int) required;
    }

    /** Resets storage per Dictionary. Failed allocation leaves existing contents intact. */
    @Override @SuppressWarnings("unchecked")
    public void ensureCapacity(int capacity) {
        Pair<K, V>[] replacement = (Pair<K, V>[]) new Pair[capacityFor(capacity)];
        values = replacement;
        count = maxProbingSteps = reallocationCount = 0;
        collisionCount = 0;
    }

    private int index(K key, int capacity) {
        return Math.floorMod(key.hashCode(), capacity);
    }

    @Override public boolean add(K key, V value) {
        if (key == null || value == null) throw new IllegalArgumentException("Key and value cannot be null");
        int slot = index(key, values.length);
        int probes = 0;
        while (values[slot] != null && probes < values.length) {
            if (values[slot].getKey().equals(key)) {
                values[slot].setValue(value);
                maxProbingSteps = Math.max(maxProbingSteps, probes);
                return true;
            }
            collisionCount++;
            probes++;
            if (++slot == values.length) slot = 0;
        }
        maxProbingSteps = Math.max(maxProbingSteps, probes);
        if (count + 1 > values.length * LOAD_FACTOR) {
            long grown = Math.max((long) capacityFor(count + 1), (long) values.length * 2);
            grown = Math.min(grown, MAX_ARRAY_SIZE);
            if (grown <= values.length) throw new OutOfMemoryError("Hash table is full");
            reallocate((int) grown);
            slot = index(key, values.length);
            probes = 0;
            while (values[slot] != null) {
                collisionCount++;
                probes++;
                if (++slot == values.length) slot = 0;
            }
            maxProbingSteps = Math.max(maxProbingSteps, probes);
        }
        values[slot] = new Pair<>(key, value);
        count++;
        return true;
    }

    @Override public V find(K key) {
        if (key == null) throw new IllegalArgumentException("Key cannot be null");
        int slot = index(key, values.length);
        for (int probes = 0; probes < values.length; probes++) {
            Pair<K, V> pair = values[slot];
            if (pair == null) return null;
            if (pair.getKey().equals(key)) return pair.getValue();
            if (++slot == values.length) slot = 0;
        }
        return null;
    }

    /** Rehash completely before publishing the replacement. Statistics are cumulative. */
    @SuppressWarnings("unchecked")
    private void reallocate(int capacity) {
        Pair<K, V>[] replacement = (Pair<K, V>[]) new Pair[capacity];
        long collisions = 0;
        int maximum = 0;
        for (Pair<K, V> pair : values) {
            if (pair == null) continue;
            int slot = index(pair.getKey(), capacity);
            int probes = 0;
            while (replacement[slot] != null) {
                probes++;
                if (++slot == capacity) slot = 0;
            }
            replacement[slot] = pair;
            collisions += probes;
            maximum = Math.max(maximum, probes);
        }
        values = replacement;
        collisionCount += collisions;
        maxProbingSteps = Math.max(maxProbingSteps, maximum);
        reallocationCount++;
    }

    @Override public void compress() {
        int capacity = capacityFor(count);
        if (capacity < values.length) reallocate(capacity);
    }

    @Override @SuppressWarnings("unchecked")
    public Pair<K, V>[] toSortedArray() {
        Pair<K, V>[] sorted = (Pair<K, V>[]) new Pair[count];
        int next = 0;
        for (Pair<K, V> pair : values) {
            if (pair != null) sorted[next++] = new Pair<>(pair.getKey(), pair.getValue());
        }
        Algorithms.fastSort(sorted);
        return sorted;
    }

    @Override public String getStatus() {
        return String.format("Hash table load factor is %.2f%nHash table capacity is %d%n"
                + "Current fill rate is %.2f%%%nHash table had %d collisions (including rehashing).%n"
                + "Hash table had to probe %d times in the worst case.%n"
                + "Hash table had to reallocate %d times.%n",
                LOAD_FACTOR, values.length, 100.0 * count / values.length,
                collisionCount, maxProbingSteps, reallocationCount);
    }
}
