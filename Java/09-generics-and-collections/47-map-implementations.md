# 47 · `Map` — `HashMap` internals and friends

> **Run the code for this lesson**
> ```bash
> java Java/09-generics-and-collections/47-map-implementations.java
> ```

Lesson 46 measured what a bad `hashCode()` costs. This lesson goes inside the structure
that cost is paid against — and produces a real, reproducible bug: a key that is still in
the map and permanently unfindable.

---

## 1. How a `HashMap` is actually built

```
table:  [0][1][2][3][4][5][6][7]  ...  capacity 16 by default
                 |
                 v
        Node(key, value, hash, next) -> Node -> null
```

Each slot holds a `Node` whose hash collided into it — a chain, in the simple case.
`put(k, v)` does three things:

1. `h = k.hashCode() ^ (k.hashCode() >>> 16)` — **spread** the bits
2. `index = h & (table.length - 1)` — fast mod, which needs a power-of-two capacity
3. Walk that bucket's chain: replace on an `equals()` match, otherwise append a new `Node`

**Why XOR the high bits in?** Step 2 only looks at the *low* bits of the hash (capacity 16
means only 4 bits matter). Many real `hashCode()` implementations vary mostly in their
*high* bits — without spreading, those differences would never affect which bucket a key
lands in, and collisions would be far more common than the hash function's quality
suggests.

```java
int h = "example".hashCode();       // -1322970774
int spread = h ^ (h >>> 16);        // -1322934193
int bucket = spread & 15;           // 15  (capacity 16)
```

---

## 2. Load factor and resizing

Default capacity 16, default load factor 0.75. **Resize threshold = capacity × load
factor.** Once size exceeds that, capacity **doubles** and every entry is rehashed into
the new, larger table:

```
capacity 16  -> resize after the 13th entry  (16 x 0.75 = 12)
capacity 32  -> resize after the 25th entry  (32 x 0.75 = 24)
capacity 64  -> resize after the 49th entry  (64 x 0.75 = 48)
... doubling, forever, unless you presize
```

A lower load factor means more memory, fewer collisions, resizes sooner. A higher one
means less memory, more collisions. 0.75 is Java's measured sweet spot.

Measured putting 2,000,000 entries:

| Constructor | Time | Resizes |
| --- | --- | --- |
| `new HashMap<>()` | 388 ms | ~21 |
| `new HashMap<>(size / 0.75 + 1)` | 273 ms | 0 |

Every resize allocates a new, double-sized array and rehashes every existing entry into
it. Presizing when the final size is known avoids all of that — same trade-off as
`ArrayList` presizing in lesson 45, for the identical reason. Note the constructor takes
the **initial capacity**, not the expected entry count — to hold `n` entries with zero
resizes, request `n / loadFactor`.

---

## 3. When a bucket becomes a tree, not a list

Since Java 8, `HashMap` does not just accept an O(n) chain forever. A bucket converts
from a linked list to a **red-black tree** when *both* of these are true (documented
constants from `HashMap`'s own source):

```
TREEIFY_THRESHOLD    = 8    <- that bucket has >= 8 nodes
MIN_TREEIFY_CAPACITY = 64   <- AND the table itself has >= 64 slots
```

If the table is still small, `HashMap` resizes instead of treeifying that one bucket —
growing the whole table first is usually enough to spread a collision out on its own.

This is precisely what softened lesson 46's `BadHashKey` disaster: 20,000 keys all
hashing to bucket 42 is far more than 8, and the table long since grew past 64 slots — so
that bucket was almost certainly a **tree** by the time lookups were measured, capping the
damage at O(log n) instead of true O(n). Even so, it was still roughly **1,000×** slower
than a good `hashCode()`. **Treeification is a safety net, not a fix** — it turns a
catastrophe into a merely bad outcome. Writing a real `hashCode()` is still the actual fix.

---

## 4. `getOrDefault`, `computeIfAbsent`, `merge`, `compute`

Word-frequency counting, three ways, over the same sentence — all three produce the same
result, `{the=3, fox=2, quick=1, brown=1, lazy=1, dog=1, runs=1}`:

```java
// the old way - TWO map lookups per word
if (map.containsKey(word)) { map.put(word, map.get(word) + 1); }
else { map.put(word, 1); }

// getOrDefault - one lookup, still an explicit put
map.put(word, map.getOrDefault(word, 0) + 1);

// merge - ONE call, no branching, the map's own idiom for this
map.merge(word, 1, Integer::sum);
```

`merge(key, 1, Integer::sum)` means: absent → `put(key, 1)`; present with value `v` →
`put(key, Integer.sum(v, 1))`.

`computeIfAbsent` is the idiom for `Map<K, List<V>>` — build-the-bucket-if-missing:

```java
map.computeIfAbsent(word.length(), key -> new ArrayList<>()).add(word);
```

Real result, grouping the sentence's words by length:
`{3=[the, fox, the, dog, the, fox], 4=[lazy, runs], 5=[quick, brown]}`. Without
`computeIfAbsent` this is three separate operations (`containsKey` check, conditional
`put` of a new list, then `get().add(...)`) — and a bug waiting to happen if any one of
them is forgotten.

---

## 5. Mutate a key after insertion and watch it vanish

`MutablePoint` (defined in the `.java` file) computes `hashCode()` from `x` and `y`, and
both fields are mutable — deliberately, to reproduce a real, common bug.

```java
Map<MutablePoint, String> pointNames = new HashMap<>();
MutablePoint origin = new MutablePoint(0, 0);
pointNames.put(origin, "origin");

pointNames.get(new MutablePoint(0, 0));   // "origin" - works so far

origin.setX(99);                          // mutate the SAME object, already a key

pointNames.get(new MutablePoint(99, 0));  // null - NOT FOUND, even though origin IS (99,0) now
pointNames.get(new MutablePoint(0, 0));   // null - NOT FOUND either, the old key is gone
pointNames.containsKey(origin);           // false - even with the SAME object reference!
pointNames.size();                        // 1 - but the entry is STILL THERE
```

Real, measured, reproducible: all three lookups genuinely fail, `containsKey` on the exact
same object reference genuinely returns `false`, and yet `entrySet()` iteration still
shows the entry sitting there — `(99, 0) = origin`.

**Why**: `put()` computed `hash(0, 0)` and filed the entry in *that* bucket. `setX(99)`
changed the object, but nothing re-files it — `HashMap` has no way to know a key changed
out from under it. A later `get()` computes `hash(99, 0)`, looks in a *different* bucket,
and finds nothing. The entry is genuinely still in the map, just permanently unreachable
by key.

**The rule**: never use a mutable object as a `HashMap`/`HashSet` key unless you can
guarantee it will never change while it's a key. Records (lesson 36, all fields `final`)
are naturally immune.

---

## 6. `LinkedHashMap`: order, and `removeEldestEntry`

Default mode preserves insertion order, exactly like `LinkedHashSet`:

```java
Map<String, Integer> m = new LinkedHashMap<>();
m.put("delta", 4); m.put("alpha", 1); m.put("charlie", 3);
// {delta=4, alpha=1, charlie=3}
```

**Access-order mode** (the 3-argument constructor's third flag) moves an entry to the end
every time it is `get()` or `put()` — the exact mechanism an LRU cache needs, and
`removeEldestEntry()` is the hook that turns it into a real, bounded cache:

```java
class LruCache<K, V> extends LinkedHashMap<K, V> {
    private final int capacity;
    LruCache(int capacity) {
        super(16, 0.75f, true);   // true = ACCESS order
        this.capacity = capacity;
    }
    @Override
    protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
        return size() > capacity;
    }
}
```

Real, traced run with capacity 3:

```
put a, b, c            -> {a=1, b=2, c=3}
get("a")                  touches a, moving it to the end
put("d", 4)             -> {c=3, a=1, d=4}
```

`"b"` was evicted — the **least recently used** entry, not `"a"` (which the `get("a")`
just before the insert had refreshed). This entire cache is `removeEldestEntry` plus
access-order mode; no manual list-splicing needed.

---

## 7. `TreeMap`: sorted keys, and `NavigableMap`

```java
NavigableMap<Integer, String> scores = new TreeMap<>();
scores.put(85, "Bob"); scores.put(92, "Alice");
scores.put(78, "Carol"); scores.put(92, "Dave");   // replaces Alice at key 92
```

Real result: `{78=Carol, 85=Bob, 92=Dave}` — Dave replaced Alice at key 92; size stayed 3.

```java
scores.firstEntry() / scores.lastEntry()    // 78=Carol / 92=Dave
scores.floorKey(90)    // <= 90             -> 85
scores.ceilingKey(90)  // >= 90             -> 92
scores.headMap(90)     // [keys < 90)       -> {78=Carol, 85=Bob}
scores.tailMap(90)     // [keys >= 90)      -> {92=Dave}
```

Exactly `TreeSet`'s `NavigableSet` API (lesson 46) — because `TreeSet` literally *is* a
`TreeMap<E, Object>` underneath, the same relationship `HashSet` has to `HashMap`.

---

## 8. Summary

- A `HashMap` is a bucket array; the key's hash is **spread** (`h ^ (h >>> 16)`) before
  the low bits pick a bucket index, so high-bit differences in `hashCode()` still matter.
- Default capacity 16, load factor 0.75, resize threshold = capacity × load factor;
  every resize **doubles** the table and rehashes everything. Presize when the final size
  is known — measured 388ms → 273ms at 2,000,000 entries, zero resizes either way beyond
  the presized allocation.
- A bucket treeifies (becomes a red-black tree) once it has ≥8 nodes *and* the table has
  ≥64 slots — this is what capped lesson 46's bad-hashCode disaster at ~1,000× instead of
  something far worse; it's a safety net, not a substitute for a real `hashCode()`.
- `merge`/`computeIfAbsent`/`getOrDefault` collapse the classic
  check-then-act-then-put pattern into one call, removing a whole class of
  forgot-a-branch bugs.
- **Never use a mutable object as a `HashMap`/`HashSet` key.** Mutating a field the
  `hashCode()` depends on makes the entry permanently unreachable by key, while it stays
  fully present in the map — confirmed here across `get`, `containsKey`, and `entrySet()`.
- `LinkedHashMap` in access-order mode plus `removeEldestEntry` is a complete, correct LRU
  cache in about ten lines — no manual bookkeeping needed.
- `TreeMap` is `HashMap`'s sorted cousin, backing `TreeSet` exactly as `HashMap` backs
  `HashSet`, with the same `NavigableMap`/`NavigableSet` API on both.

---

**Previous:** [46 — `Set` implementations](46-set-implementations.md) ·
**Next:** [48 — `Queue`, `Deque` and `PriorityQueue`](48-queue-and-deque.md)
