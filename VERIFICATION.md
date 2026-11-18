# Assignment 5 verification

Verified on 18 November 2026 using Oracle JDK 24.0.1 and Maven 3.9.9,
compiling for Java 18 as configured by the unchanged pom.xml.

## Implementation

- Algorithms.java reuses the completed Assignment 4 quicksort and its helpers
  from codax21272/DSA-04-binarysearch. The supplied partitionByRule method and
  a swap helper were added, plus comparator insertion sort for ComparatorTester.
- Person hashes the full name using the DJB2 XOR recurrence (seed 5381,
  multiply by 33 then XOR each UTF-16 character). Using exactly the full name
  preserves the existing equals/compareTo contract, including spaces inside names.
- The hash table uses plain arrays, linear probing, a 0.65 maximum load, and
  doubling growth. floorMod handles all signed hashes, including MIN_VALUE.
  Rehashing finishes before replacement storage is published. Impossible capacity
  requests fail before changing contents. Successful ensureCapacity resets the
  dictionary as explicitly specified in Dictionary.java.
- The BST reuses the supplied hash-ordered TreeNode and linked collision chains.
  Duplicate updates in collision chains no longer inflate the count. Export uses
  the supplied visitor and quicksort; empty export and status are safe. Depth is
  measured in edges; collision-chain status is specific to each tree.

No Collection/Map containers or library sorting algorithms were introduced.
The existing comparator demo uses Arrays.toString only for display.
The instructor's test sources and PhoneBook.txt are unchanged.

## Correctness

The required classes were first run separately in the requested order using
`mvn -B -Dtest=CLASS test`. PhoneBookTests ran as a complete class with
`'-Dsurefire.timeout=120' '-Dsurefire.exitTimeout=10'` to bound a possible hang.

The final combined verification command was:

```powershell
mvn -B '-Dtest=GenericTests,ContentsTests,HashFuncTests,PhoneBookTests' '-Dsurefire.timeout=120' test
```

| Class | Executed | Failures | Errors | Skipped |
|---|---:|---:|---:|---:|
| GenericTests | 4 | 0 | 0 | 0 |
| ContentsTests | 4 | 0 | 0 | 0 |
| HashFuncTests | 2 | 0 | 0 | 0 |
| PhoneBookTests | 3 | 0 | 0 | 0 |
| Total | 13 | 0 | 0 | 0 |

Surefire XML reports were inspected. Both HASHTABLE and BST executed; their
getType methods return their implemented types. JaCoCo reports no execution data
under the supplied configuration; no coverage claim is made.

Additional verification, without changing instructor tests:

```powershell
javac -encoding UTF-8 -cp target/classes -d target/verification verification/DictionaryChecks.java
java -cp 'target/classes;target/verification' DictionaryChecks
```

Passed for both implementations: empty dictionaries, 400 heavily colliding keys,
MIN_VALUE/negative/MAX_VALUE hashes, duplicate collision-chain replacement,
compression, sorted exports, export independence, absent keys, null arguments,
Person identity boundaries, and hash table allocation-failure preservation.

## Observed performance

Final combined correctness run, using the original 12,345-entry PhoneBook.txt:

| Operation | Linear array | Hash table | BST |
|---|---:|---:|---:|
| Read and insert (ms) | 3148.0144 | 13.6949 | 17.1079 |
| Six successful lookups total (ms) | 6.4896 | 0.0218 | 0.0428 |
| Lookup speed relative to linear | 1 | 297.69 | 151.63 |

The separate PhoneBookTests run also passed: lookup totals were 54.9505 ms,
0.0462 ms and 0.0507 ms respectively. These small, cold measurements vary with
JIT compilation and scheduling; they demonstrate passing the supplied threshold,
not a statistically controlled benchmark. Insertion includes file parsing.

The hash table had capacity 19,968, fill 61.82%, 22,898 cumulative probe
collisions including rehashing, worst probe distance 50, and 9 reallocations.
The BST's longest root-to-leaf path was 33 edges (balanced ideal 13), shortest
leaf path 5, and no extra collision-chain entries for this input.

Expected hash-table insertion/lookup is amortized O(1) with a good hash, with
O(n) worst-case probing and O(n) storage. Growth rehashes entries; compression
reduces unused slots while respecting the load threshold. A hash collision here
means an occupied probe slot, which does not necessarily mean equal hash codes.
The BST is unbalanced: expected O(log n) operations for well-distributed hashes,
O(n) worst case, with linear scans for equal-hash chains. The supplied recursive
tree helpers can exhaust the stack for pathological tree depths. Export is
O(n) copying plus average O(n log n) quicksort (worst O(n squared)); quicksort
recurses only into the smaller partition to bound sorting stack use to O(log n).

One hash function and one load threshold were tested; no comparative tuning or
quicksort-versus-heapsort measurement is claimed. No heap/stack overrides were
needed for the available correctness data.

The external towns datasets are not present locally. Large-dataset performance
tests and a size-versus-time graph remain dependent on obtaining towns.zip.
