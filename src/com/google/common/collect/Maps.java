/*
 * Copyright (C) 2007 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.common.collect;

import com.google.common.base.Function;
import com.google.common.base.Nullable;
import com.google.common.base.Objects;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Provides static methods for creating mutable {@link Map} instances easily.
 * You can replace code like:
 *
 * <p>{@code Map<String, Integer> map = new HashMap<String, Integer>();}
 *
 * <p>with just:
 *
 * <p>{@code Map<String, Integer> map = Maps.newHashMap();}
 *
 * <p>Supported today are: {@link HashMap}, {@link LinkedHashMap},
 * {@link ConcurrentHashMap}, {@link TreeMap}, and {@link EnumMap}.
 *
 * <p>See also this class's counterparts {@link Lists} and {@link Sets}.
 *
 * <p>WARNING: These factories do not support the full variety of tuning
 * parameters available in the collection constructors.  Use them only for
 * collections which will always remain small, or for which the cost of future
 * growth operations is not a concern.
 *
 * @author kevinb@google.com (Kevin Bourrillion)
 * @author mbostock@google.com (Mike Bostock)
 */
@SuppressWarnings({
    "CollectionDeclaredAsConcreteClass",
    "CollectionWithoutInitialCapacity"})
public final class Maps {
  private static final BiMap<?,?> EMPTY_BIMAP
      = new EmptyBiMap<Object, Object>();

  private Maps() {}

  /**
   * Creates a {@code HashMap} instance.  <b>Notes:</b> if {@code K} is an
   * {@link Enum} type, use {@link #newEnumMap} instead.  If you only need an
   * <i>immutable</i> empty Map, use {@link Collections#emptyMap} instead.
   *
   * @return a newly-created, initially-empty {@code HashMap}
   */
  public static <K,V> HashMap<K,V> newHashMap() {
    return new HashMap<K,V>();
  }

  /**
   * Informally deprecated; replaced by {@link #newHashMapWithExpectedSize}.
   * <p>
   * Creates a {@code HashMap} instance.  <b>Notes:</b> if {@code K} is an
   * {@link Enum} type, use {@link #newEnumMap} instead.
   *
   * @param initialCapacity the initial capacity
   * @return a newly-created {@code HashMap}, initially-empty, with the
   * specified capacity
   */
  public static <K,V> HashMap<K,V> newHashMap(int initialCapacity) {
    return new HashMap<K,V>(initialCapacity);
  }

  /**
   * Creates a {@code HashMap} instance with enough capacity to hold the
   * specified number of elements without rehashing.
   *
   * @param expectedSize the expected size
   * @return a newly-created {@code HashMap}, initially-empty, with the
   * enough capacity to hold {@code expectedSize} elements without
   * rehashing.
   * @throws IllegalArgumentException if {@code expectedSize} is negative
   */
  public static <K,V> HashMap<K,V> newHashMapWithExpectedSize(
      int expectedSize) {
    return new HashMap<K,V>(capacity(expectedSize));
  }

  /**
   * Returns the appropriate initial capacity for a new {@code HashMap} to hold
   * the specified number of entries, given the default load factor of 0.75.
   *
   * @throws IllegalArgumentException if {@code expectedSize} is negative
   */
  static int capacity(int expectedSize) {
    checkArgument(expectedSize >= 0);
    return Math.max(expectedSize + expectedSize / 3 + 1, 16);
  }

  /**
   * Creates a {@code HashMap} instance with the same mappings as the
   * specified map. <b>Notes:</b> if {@code K} is an {@link Enum} type, use
   * {@link #newEnumMap} instead. If you only need an <i>immutable</i> empty
   * Map, use {@link Collections#emptyMap} instead.
   *
   * @param map the mappings to be placed in the new map
   * @return a newly-created, initially-empty {@code HashMap}
   */
  public static <K,V> HashMap<K,V> newHashMap(Map<K,V> map) {
    return new HashMap<K,V>(map);
  }

  /**
   * Creates a {@code LinkedHashMap} instance.
   *
   * @return a newly-created, initially-empty {@code LinkedHashMap}
   */
  public static <K,V> LinkedHashMap<K,V> newLinkedHashMap() {
    return new LinkedHashMap<K,V>();
  }

  /**
   * Creates a {@code ConcurrentHashMap} instance.
   *
   * @return a newly-created, initially-empty {@code ConcurrentHashMap}
   */
  public static <K,V> ConcurrentHashMap<K,V> newConcurrentHashMap() {
    return new ConcurrentHashMap<K,V>();
  }

  /**
   * Creates a {@code TreeMap} instance using the default {@link Comparator}.
   * <b>Note:</b> If {@code K} is an {@link Enum} type, and you don't require
   * the map to implement {@link SortedMap} (only ordered iteration), use
   * {@link #newEnumMap} instead.
   *
   * @return a newly-created, initially-empty {@code TreeMap}
   */
  public static <K extends Comparable<K>, V> TreeMap<K,V> newTreeMap() {
    return new TreeMap<K,V>();
  }

  /**
   * Creates a {@code TreeMap} instance using the given comparator.
   *
   * @param comparator the Comparator to sort the keys with
   * @return a newly-created, initially-empty {@code TreeMap}
   */
  public static <K,V> TreeMap<K,V> newTreeMap(
      @Nullable Comparator<? super K> comparator) {
    return new TreeMap<K,V>(comparator);
  }

  /**
   * Creates an {@code EnumMap} instance.
   *
   * @param type the key type for this map
   * @return a newly-created, initially-empty {@code EnumMap}
   */
  public static <K extends Enum<K>, V> EnumMap<K,V> newEnumMap(Class<K> type) {
    checkNotNull(type);
    return new EnumMap<K,V>(type);
  }

  /**
   * Creates a {@code IdentityHashMap} instance.
   *
   * @return a newly-created, initially-empty {@code IdentityHashMap}
   */
  public static <K,V> IdentityHashMap<K,V> newIdentityHashMap() {
    return new IdentityHashMap<K,V>();
  }

  /**
   * Returns true if {@code map} contains an entry mapping {@code key} to
   * {@code value}.  If you are not concerned with null-safety you can simply
   * use {@code map.get(key).equals(value)}.
   */
  public static boolean containsEntry(
      Map<?,?> map, @Nullable Object key, @Nullable Object value) {
    checkNotNull(map);
    Object valueForKey = map.get(key);
    return (valueForKey == null)
        ? value == null && map.containsKey(key)
        : valueForKey.equals(value);
  }

  /**
   * Creates a new immutable empty {@code Map} instance.
   *
   * @see Collections#emptyMap
   */
  public static <K,V> Map<K,V> immutableMap() {
    return Collections.emptyMap();
  }

  /**
   * Creates a new immutable {@code Map} instance containing the given key-value
   * pair.
   *
   * @see Collections#singletonMap
   */
  public static <K,V> Map<K,V> immutableMap(
      @Nullable K k1, @Nullable V v1) {
    return Collections.singletonMap(k1, v1);
  }

  /**
   * Creates a new immutable {@code Map} instance containing the given key-value
   * pairs.
   *
   * @see ImmutableMapBuilder
   */
  public static <K,V> Map<K,V> immutableMap(
      @Nullable K k1, @Nullable V v1,
      @Nullable K k2, @Nullable V v2) {
    return new ImmutableMapBuilder<K,V>()
        .put(k1, v1)
        .put(k2, v2)
        .getMap();
  }

  /**
   * Creates a new immutable {@code Map} instance containing the given key-value
   * pairs.
   *
   * @see ImmutableMapBuilder
   */
  public static <K,V> Map<K,V> immutableMap(
      @Nullable K k1, @Nullable V v1,
      @Nullable K k2, @Nullable V v2,
      @Nullable K k3, @Nullable V v3) {
    return new ImmutableMapBuilder<K,V>()
        .put(k1, v1)
        .put(k2, v2)
        .put(k3, v3)
        .getMap();
  }

  /**
   * Creates a new immutable {@code Map} instance containing the given key-value
   * pairs.
   *
   * @see ImmutableMapBuilder
   */
  public static <K,V> Map<K,V> immutableMap(
      @Nullable K k1, @Nullable V v1,
      @Nullable K k2, @Nullable V v2,
      @Nullable K k3, @Nullable V v3,
      @Nullable K k4, @Nullable V v4) {
    return new ImmutableMapBuilder<K,V>()
        .put(k1, v1)
        .put(k2, v2)
        .put(k3, v3)
        .put(k4, v4)
        .getMap();
  }

  /**
   * Creates a new immutable {@code Map} instance containing the given key-value
   * pairs.
   *
   * @see ImmutableMapBuilder
   */
  public static <K,V> Map<K,V> immutableMap(
      @Nullable K k1, @Nullable V v1,
      @Nullable K k2, @Nullable V v2,
      @Nullable K k3, @Nullable V v3,
      @Nullable K k4, @Nullable V v4,
      @Nullable K k5, @Nullable V v5) {
    return new ImmutableMapBuilder<K,V>()
        .put(k1, v1)
        .put(k2, v2)
        .put(k3, v3)
        .put(k4, v4)
        .put(k5, v5)
        .getMap();
  }

  /*
   * Please use ImmutableMapBuilder directly if you are looking for overloads
   * for 6 or more key-value pairs.
   */

  /**
   * Creates a new immutable empty {@code BiMap} instance.
   *
   * @see #emptyBiMap
   */
  public static <K,V> BiMap<K,V> immutableBiMap() {
    return emptyBiMap();
  }

  /**
   * Creates a new immutable {@code BiMap} instance containing the given
   * key-value pair.
   *
   * @see #singletonBiMap
   */
  public static <K,V> BiMap<K,V> immutableBiMap(
      @Nullable K k1, @Nullable V v1) {
    return singletonBiMap(k1, v1);
  }

  /**
   * Creates a new immutable {@code BiMap} instance containing the given
   * key-value pairs.
   *
   * @see ImmutableBiMapBuilder
   */
  public static <K,V> BiMap<K,V> immutableBiMap(
      @Nullable K k1, @Nullable V v1,
      @Nullable K k2, @Nullable V v2) {
    return new ImmutableBiMapBuilder<K,V>()
        .put(k1, v1)
        .put(k2, v2)
        .getBiMap();
  }

  /**
   * Creates a new immutable {@code BiMap} instance containing the given
   * key-value pairs.
   *
   * @see ImmutableBiMapBuilder
   */
  public static <K,V> BiMap<K,V> immutableBiMap(
      @Nullable K k1, @Nullable V v1,
      @Nullable K k2, @Nullable V v2,
      @Nullable K k3, @Nullable V v3) {
    return new ImmutableBiMapBuilder<K,V>()
        .put(k1, v1)
        .put(k2, v2)
        .put(k3, v3)
        .getBiMap();
  }

  /**
   * Creates a new immutable {@code BiMap} instance containing the given
   * key-value pairs.
   *
   * @see ImmutableBiMapBuilder
   */
  public static <K,V> BiMap<K,V> immutableBiMap(
      @Nullable K k1, @Nullable V v1,
      @Nullable K k2, @Nullable V v2,
      @Nullable K k3, @Nullable V v3,
      @Nullable K k4, @Nullable V v4) {
    return new ImmutableBiMapBuilder<K,V>()
        .put(k1, v1)
        .put(k2, v2)
        .put(k3, v3)
        .put(k4, v4)
        .getBiMap();
  }

  /**
   * Creates a new immutable {@code BiMap} instance containing the given
   * key-value pairs.
   *
   * @see ImmutableBiMapBuilder
   */
  public static <K,V> BiMap<K,V> immutableBiMap(
      @Nullable K k1, @Nullable V v1,
      @Nullable K k2, @Nullable V v2,
      @Nullable K k3, @Nullable V v3,
      @Nullable K k4, @Nullable V v4,
      @Nullable K k5, @Nullable V v5) {
    return new ImmutableBiMapBuilder<K,V>()
        .put(k1, v1)
        .put(k2, v2)
        .put(k3, v3)
        .put(k4, v4)
        .put(k5, v5)
        .getBiMap();
  }

  /*
   * Please use ImmutableBiMapBuilder directly if you are looking for overloads
   * for 6 or more key-value pairs.
   */

  /**
   * Returns a synchronized (thread-safe) bimap backed by the specified
   * bimap. In order to guarantee serial access, it is critical that <b>all</b>
   * access to the backing bimap is accomplished through the returned bimap.
   *
   * <p>It is imperative that the user manually synchronize on the returned map
   * when accessing any of its collection views:
   *
   * <pre>  Bimap&lt;K,V> m = Maps.synchronizedBiMap(
   *      new HashBiMap&lt;K,V>());
   *   ...
   *  Set&lt;K> s = m.keySet();  // Needn't be in synchronized block
   *   ...
   *  synchronized (m) {  // Synchronizing on m, not s!
   *    Iterator&lt;K> i = s.iterator(); // Must be in synchronized block
   *    while (i.hasNext()) {
   *      foo(i.next());
   *    }
   *  }</pre>
   *
   * Failure to follow this advice may result in non-deterministic behavior.
   *
   * @param bimap the bimap to be wrapped in a synchronized view
   * @return a sychronized view of the specified bimap
   */
  public static <K,V> BiMap<K,V> synchronizedBiMap(BiMap<K,V> bimap) {
    return Synchronized.biMap(bimap, null);
  }

  /**
   * Returns a sorted set view of the keys contained in the specified map. The
   * set is backed by the map, so changes to the map are reflected in the set,
   * and vice-versa. If the map is modified while an iteration over the set is
   * in progress (except through the iterator's own {@code remove} operation),
   * the results of the iteration are undefined. The set supports element
   * removal, which removes the corresponding mapping from the map, via the
   * {@code Iterator.remove}, {@code Set.remove}, {@code removeAll}, {@code
   * removeAll}, and {@code clear} operations. It does not support the add or
   * {@code addAll} operations.
   *
   * @return a sorted set view of the keys contained in the specified map.
   */
  public static <K,V> SortedSet<K> sortedKeySet(SortedMap<K,V> map) {
    return new SortedMapKeySet<K,V>(map);
  }

  private static class SortedMapKeySet<K,V> extends ForwardingSet<K>
      implements SortedSet<K> {
    private final SortedMap<K,V> map;
    public SortedMapKeySet(SortedMap<K,V> map) {
      super(map.keySet());
      this.map = map;
    }
    public Comparator<? super K> comparator() {
      return map.comparator();
    }
    public K first() {
      return map.firstKey();
    }
    public SortedSet<K> headSet(K toElement) {
      return new SortedMapKeySet<K,V>(map.headMap(toElement));
    }
    public K last() {
      return map.lastKey();
    }
    public SortedSet<K> subSet(K fromElement, K toElement) {
      return new SortedMapKeySet<K,V>(map.subMap(fromElement, toElement));
    }
    public SortedSet<K> tailSet(K fromElement) {
      return new SortedMapKeySet<K,V>(map.tailMap(fromElement));
    }
  }

  /**
   * Creates an index {@code Map} that contains the results of applying a
   * specified function to each item in an {@code Iterable} of values. Each
   * value will be stored as a value in the resulting map.  The key
   * used to store that value in the map will be the result of calling the
   * function on that value.  Neither keys nor values are allowed to be
   * {@code null}.  It is an error if the function produces the
   * same key for more than one value in the input collection.
   *
   * @param values the values to use when constructing the {@code Map}
   * @param keyFunction the function used to produce the key for each value
   * @return {@code Map} mapping the result of evaluating the
   *    function {@code keyFunction} on each value in the input collection to
   *    that value
   * @throws IllegalArgumentException if {@code keyFunction} produces the same
   *    key for more than one value in the input collection
   * @throws NullPointerException if any elements of {@code values} are
   *    {@code null} or if {@code keyFunction} produces {@code null} for any
   *    value
   */
  public static <K,V> Map<K,V> uniqueIndex(
      Iterable<? extends V> values,
      Function<? super V, ? extends K> keyFunction) {
    HashMap<K,V> newMap;
    if (values instanceof Collection<?>) {
      // If it's really a collection, take advantage of knowing the size
      Collection<? extends V> collection = (Collection<? extends V>) values;
      newMap = new HashMap<K,V>(collection.size());
    } else {
      newMap = new HashMap<K,V>();
    }
    return uniqueIndex(newMap, values.iterator(), keyFunction);
  }

  /**
   * Creates an index {@code Map} that contains the results of applying a
   * specified function to each item in a {@code Collection} of values. Each
   * value will be stored as a value in the resulting map.  The key
   * used to store that value in the map will be the result of calling the
   * function on that value.  Neither keys nor values are allowed to be
   * {@code null}.  It is an error if the function produces the
   * same key for more than one value in the input collection.
   *
   * @param values the values to use when constructing the {@code Map}
   * @param keyFunction the function used to produce the key for each value
   * @return {@code Map} mapping the result of evaluating the
   *    function {@code keyFunction} on each value in the input collection to
   *    that value
   * @throws IllegalArgumentException if {@code keyFunction} produces the same
   *    key for more than one value in the input collection
   * @throws NullPointerException if any elements of {@code values} are
   *    {@code null} or if {@code keyFunction} produces {@code null} for any
   *    value
   */
  public static <K,V> Map<K,V> uniqueIndex(
      Collection<? extends V> values,
      Function<? super V, ? extends K> keyFunction) {
    return uniqueIndex(new HashMap<K,V>(values.size() * 2),
                       values.iterator(), keyFunction);
  }

  /**
   * Creates an index {@code Map} that contains the results of applying a
   * specified function to each item in an {@code Iterator} of values. Each
   * value will be stored as a value in the resulting map.  The key
   * used to store that value in the map will be the result of calling the
   * function on that value.  Neither keys nor values are allowed to be
   * {@code null}.  It is an error if the function produces the
   * same key for more than one value in the input collection.
   *
   * @param values the values to use when constructing the {@code Map}
   * @param keyFunction the function used to produce the key for each value
   * @return {@code Map} mapping the result of evaluating the
   *    function {@code keyFunction} on each value in the input collection to
   *    that value
   * @throws IllegalArgumentException if {@code keyFunction} produces the same
   *    key for more than one value in the input collection
   * @throws NullPointerException if any elements of {@code values} are
   *    {@code null} or if {@code keyFunction} produces {@code null} for any
   *    value
   */
  public static <K,V> Map<K,V> uniqueIndex(
      Iterator<? extends V> values,
      Function<? super V, ? extends K> keyFunction) {
    return uniqueIndex(new HashMap<K,V>(), values, keyFunction);
  }

  private static <K,V> Map<K,V> uniqueIndex(
      Map<K,V> map,
      Iterator<? extends V> values,
      Function<? super V, ? extends K> keyFunction) {
    checkNotNull(keyFunction, "function cannot be null");
    while (values.hasNext()) {
      V value = values.next();
      checkNotNull(value, "null index values not allowed");
      K key = keyFunction.apply(value);
      checkNotNull(key, "null index keys not allowed");
      if (map.put(key, value) != null) {
        throw new IllegalArgumentException("Duplicate key: " + key);
      }
    }
    return map;
  }

  /**
   * Creates a {@code Map<String, String>} from a {@link Properties}
   * object.  Properties normally derive from {@code Map<Object,
   * Object>}, but they typically contain strings, which is awkward.
   * This method lets you get a plain-old-{@code Map} out of a {@code
   * Properties}.  Note that you won't be able to save the changes to
   * the Map the way you can with {@link
   * Properties#store(java.io.OutputStream,String)}.  Most people don't do this
   * anyway.
   *
   * @param prop A non-null {@code Properties} object to be converted.
   * @return A {@code Map<String, String>} containing all the entries
   * in the Properties object.  Note that {@code Properties} does not
   * allow the key or value to be {@code null}.
   */
  public static Map<String, String> fromProperties(Properties prop) {
    checkNotNull(prop);
    Map<String, String> ret = newHashMap(prop.size() * 2);
    for (Enumeration e = prop.propertyNames(); e.hasMoreElements(); ) {
      Object k = e.nextElement();

      // It is unlikely that a 'null' could be inserted into a
      // Properties, but possible in a derived class.
      String key = (k != null) ? k.toString() : null;
      ret.put(key, prop.getProperty(key));
    }
    return ret;
  }

  /**
   * Returns an immutable map entry with the specified key and value. The {@link
   * Entry#setValue} operation throws an {@link UnsupportedOperationException}.
   *
   * <p>The returned entry is serializable.
   *
   * @param key the key to be associated with the returned entry
   * @param value the value to be associated with the returned entry
   */
  public static <K,V> Entry<K,V> immutableEntry(@Nullable final K key,
      @Nullable final V value) {
    return new ImmutableEntry<K,V>(key, value);
  }

  /** @see #immutableEntry */
  static class ImmutableEntry<K,V> extends AbstractMapEntry<K,V>
      implements Serializable {
    private static final long serialVersionUID = 8715539841043489689L;
    private final K key;
    private final V value;

    public ImmutableEntry(K key, V value) {
      this.key = key;
      this.value = value;
    }

    public K getKey() {
      return key;
    }
    public V getValue() {
      return value;
    }
  }

  /**
   * Returns an unmodifiable view of the specified set of entries. The {@link
   * Entry#setValue} operation throws an {@link UnsupportedOperationException},
   * as do any operations that would modify the returned set.
   *
   * @param entries the entries for which to return an unmodifiable view
   * @return an unmodifiable view of the entries
   * @throws NullPointerException if {@code entries} is null
   */
  static <K,V> Set<Entry<K,V>> unmodifiableEntrySet(
      final Set<Entry<K,V>> entrySet) {
    checkNotNull(entrySet);
    return new UnmodifiableEntrySet<K,V>(Collections.unmodifiableSet(entrySet));
  }

  /**
   * Returns an unmodifiable view of the specified map entry. The {@link
   * Entry#setValue} operation throws an {@link UnsupportedOperationException}.
   * This also has the side-effect of redefining {@code equals} to comply with
   * the Entry contract, to avoid a possible nefarious implementation of equals.
   *
   * @param entry the entry for which to return an unmodifiable view
   * @return an unmodifiable view of the entry
   * @throws NullPointerException if {@code entry} is null
   */
  static <K,V> Entry<K,V> unmodifiableEntry(final Entry<K,V> entry) {
    checkNotNull(entry);
    return new AbstractMapEntry<K,V>() {
        public K getKey() {
          return entry.getKey();
        }
        public V getValue() {
          return entry.getValue();
        }
      };
  }

  /** @see Multimaps#unmodifiableEntries */
  static class UnmodifiableEntries<K,V>
      extends ForwardingCollection<Entry<K,V>> {
    UnmodifiableEntries(Collection<Entry<K,V>> entries) {
      super(entries);
    }
    @Override public Iterator<Entry<K,V>> iterator() {
      return new ForwardingIterator<Entry<K,V>>(super.iterator()) {
          public Entry<K,V> next() {
            return unmodifiableEntry(super.next());
          }
        };
    }

    /* See java.util.Collections.UnmodifiableEntrySet for details on attacks. */
    @Override public Object[] toArray() {
      return toArrayImpl(this);
    }
    @Override public <T> T[] toArray(T[] array) {
      return toArrayImpl(this, array);
    }
    @Override public boolean contains(Object o) {
      return containsEntryImpl(delegate(), o);
    }
    @Override public boolean containsAll(Collection<?> c) {
      return containsAllImpl(this, c);
    }
  }

  /** @see #unmodifiableEntrySet */
  static class UnmodifiableEntrySet<K,V>
      extends UnmodifiableEntries<K,V>
      implements Set<Entry<K,V>> {
    UnmodifiableEntrySet(Set<Entry<K,V>> entries) {
      super(entries);
    }

    /* See java.util.Collections.UnmodifiableEntrySet for details on attacks. */
    @Override public boolean equals(Object o) {
      return ForwardingSet.equalsImpl(this, o);
    }
  }

  /**
   * Returns a new empty {@code HashBiMap} with the default initial capacity
   * (16) and load factor (0.75).
   */
  public static <K,V> HashBiMap<K,V> newHashBiMap() {
    return new HashBiMap<K,V>();
  }

  /**
   * Returns a new empty {@code EnumHashBiMap} using the specified key type,
   * sized to contain an entry for every possible key.
   *
   * @param keyType the key type
   * @throws NullPointerException if any argument is null
   */
  public static <K extends Enum<K>, V> EnumHashBiMap<K,V> newEnumHashBiMap(
      Class<K> keyType) {
    return new EnumHashBiMap<K,V>(keyType);
  }

  /**
   * Returns a new empty {@code EnumBiMap} using the specified key type and
   * value type.
   *
   * @param keyType the key type
   * @param valueType the value type
   * @throws NullPointerException if any argument is null
   */
  public static <K extends Enum<K>, V extends Enum<V>> EnumBiMap<K,V>
      newEnumBiMap(Class<K> keyType, Class<V> valueType) {
    return new EnumBiMap<K,V>(keyType, valueType);
  }

  /**
   * Returns a new {@code BiMap}, backed by the two supplied empty maps. The
   * caller surrenders control of these maps and should not retain any
   * references to either.
   *
   * <p>The returned bimap will be serializable if both of the specified maps
   * are serializable.
   *
   * @param forward an empty map to be used for associating keys with values
   * @param backward an empty map to be used for associating values with keys
   * @throws IllegalArgumentException if either map is nonempty
   */
  public static <K,V> BiMap<K,V> newBiMap(Map<K,V> forward, Map<V,K> backward) {
    return new StandardBiMap<K,V>(forward, backward);
  }

  /**
   * Returns an unmodifiable view of the specified bimap. This method allows
   * modules to provide users with "read-only" access to internal bimaps. Query
   * operations on the returned bimap "read through" to the specified bimap, and
   * attemps to modify the returned map, whether direct or via its collection
   * views, result in an {@code UnsupportedOperationException}.
   *
   * <p>The returned bimap will be serializable if the specified map is
   * serializable.
   *
   * @param bimap the bimap for which an unmodifiable view is to be returned
   * @return an unmodifiable view of the specified bimap
   */
  public static <K,V> BiMap<K,V> unmodifiableBiMap(BiMap<K,V> bimap) {
    checkNotNull(bimap);
    return new UnmodifiableBiMap<K,V>(bimap, null);
  }

  /** @see #unmodifiableBiMap */
  static class UnmodifiableBiMap<K,V> extends ForwardingMap<K,V>
      implements BiMap<K,V> {
    private static final long serialVersionUID = 9106827410356097381L;
    private final BiMap<K,V> delegate;
    private transient volatile BiMap<V,K> inverse;
    UnmodifiableBiMap(BiMap<K,V> delegate, BiMap<V,K> inverse) {
      super(Collections.unmodifiableMap(delegate));
      this.delegate = delegate;
      this.inverse = inverse;
    }
    public V forcePut(K key, V value) {
      throw new UnsupportedOperationException();
    }
    public BiMap<V,K> inverse() {
      if (inverse == null) {
        inverse = new UnmodifiableBiMap<V,K>(delegate.inverse(), this);
      }
      return inverse;
    }
    @Override public Set<V> values() {
      return Collections.unmodifiableSet(delegate.values());
    }
  }

  /**
   * Returns an immutable bimap, mapping only the specified key to the specified
   * value. The returned map is serializable if the specified key and value are
   * serializable.
   *
   * @param key the sole key to be stored in the returned bimap
   * @param value the sole value to be stored in the returned bimap
   */
  public static <K,V> BiMap<K,V> singletonBiMap(@Nullable K key,
      @Nullable V value) {
    return new SingletonBiMap<K,V>(key, value, null);
  }

  /** @see #singletonBiMap */
  static class SingletonBiMap<K,V> implements BiMap<K,V>, Serializable {
    private final K key;
    private final V value;
    private transient volatile BiMap<V,K> inverse;
    private transient volatile Set<Entry<K,V>> entrySet;
    private transient volatile Set<K> keySet;
    private transient volatile Set<V> values;

    public SingletonBiMap(K key, V value, BiMap<V,K> inverse) {
      this.key = key;
      this.value = value;
      this.inverse = inverse;
    }

    /* BiMap methods */
    public V forcePut(K key, V value) {
      throw new UnsupportedOperationException();
    }
    public BiMap<V,K> inverse() {
      if (inverse == null) {
        inverse = new SingletonBiMap<V,K>(value, key, this);
      }
      return inverse;
    }

    /* Map methods */
    public void clear() {
      throw new UnsupportedOperationException();
    }
    public boolean containsKey(Object key) {
      return Objects.equal(this.key, key);
    }
    public boolean containsValue(Object value) {
      return Objects.equal(this.value, value);
    }
    public Set<Entry<K,V>> entrySet() {
      if (entrySet == null) {
        entrySet = Collections.singleton(immutableEntry(key, value));
      }
      return entrySet;
    }
    public V get(Object key) {
      return containsKey(key) ? value : null;
    }
    public boolean isEmpty() {
      return false;
    }
    public Set<K> keySet() {
      if (keySet == null) {
        keySet = Collections.singleton(key);
      }
      return keySet;
    }
    public V put(K key, V value) {
      throw new UnsupportedOperationException();
    }
    public void putAll(Map<? extends K, ? extends V> t) {
      throw new UnsupportedOperationException();
    }
    public V remove(Object key) {
      throw new UnsupportedOperationException();
    }
    public int size() {
      return 1;
    }
    public Set<V> values() {
      if (values == null) {
        values = Collections.singleton(value);
      }
      return values;
    }

    /* Object methods */
    @Override public boolean equals(Object o) {
      if (o == this) {
        return true;
      }
      if (!(o instanceof Map<?,?>)) {
        return false;
      }
      return entrySet().equals(((Map<?,?>) o).entrySet());
    }
    @Override public int hashCode() {
      return ((key == null) ? 0 : key.hashCode())
          ^ ((value == null) ? 0 : value.hashCode());
    }
    @Override public String toString() {
      return "{" + key + "=" + value + "}";
    }
  }

  /** Returns the empty bimap (immutable). This bimap is serializable. */
  @SuppressWarnings("unchecked")
  public static <K,V> BiMap<K,V> emptyBiMap() {
    return (BiMap) EMPTY_BIMAP;
  }

  /** @see #emptyBiMap */
  static class EmptyBiMap<K,V> implements BiMap<K,V>, Serializable {

    /* BiMap methods */
    public V forcePut(K key, V value) {
      throw new UnsupportedOperationException();
    }
    @SuppressWarnings("unchecked")
    public BiMap<V,K> inverse() {
      return (BiMap) this;
    }

    /* Map methods */
    public void clear() {
      throw new UnsupportedOperationException();
    }
    public boolean containsKey(Object key) {
      return false;
    }
    public boolean containsValue(Object value) {
      return false;
    }
    public Set<Entry<K,V>> entrySet() {
      return Collections.emptySet();
    }
    public V get(Object key) {
      return null;
    }
    public boolean isEmpty() {
      return true;
    }
    public Set<K> keySet() {
      return Collections.emptySet();
    }
    public V put(K key, V value) {
      throw new UnsupportedOperationException();
    }
    public void putAll(Map<? extends K, ? extends V> t) {
      throw new UnsupportedOperationException();
    }
    public V remove(Object key) {
      throw new UnsupportedOperationException();
    }
    public int size() {
      return 0;
    }
    public Set<V> values() {
      return Collections.emptySet();
    }

    /* Object methods */
    @Override public boolean equals(Object o) {
      if (o == this) {
        return true;
      }
      if (!(o instanceof Map<?,?>)) {
        return false;
      }
      return ((Map<?,?>) o).isEmpty();
    }
    public String toString() {
      return "{}";
    }
    public int hashCode() {
      return 0;
    }

    private Object readResolve() {
      return EMPTY_BIMAP; // preserve singleton property
    }
  }

  /**
   * Returns a dynamically typesafe view of the specified bimap. Any attempt to
   * insert a mapping whose key or value have the wrong type will result in an
   * immediate {@code ClassCastException}. Similarly, any attempt to modify the
   * value currently associated with a key will result in an immediate {@code
   * ClassCastException} if the value has the wrong type, whether the
   * modification is attempted directly through the bimap itself, or through one
   * of its collection views.
   *
   * <p>Assuming the bimap contains no incorrectly typed keys or values prior to
   * the time a dynamically typesafe view is generated, and that all subsequent
   * access to the bimap takes place through the view (or one of its collection
   * views), it is <i>guaranteed</i> that the bimap cannot contain an
   * incorrectly typed key or value.
   *
   * <p>A discussion of the use of dynamically typesafe views may be found in
   * the documentation for the {@link Collections#checkedCollection
   * checkedCollection} method.
   *
   * <p>The returned bimap will be serializable if the specified bimap is
   * serializable.
   *
   * @param m the bimap for which a dynamically typesafe view is to be returned
   * @param keyType the type of key that {@code m} is permitted to hold
   * @param valueType the type of value that {@code m} is permitted to hold
   * @return a dynamically typesafe view of the specified bimap
   */
  public static <K,V> BiMap<K,V> checkedBiMap(BiMap<K,V> m,
      Class<K> keyType, Class<V> valueType) {
    return MapConstraints.constrainedBiMap(m,
        MapConstraints.classConstraint(keyType, valueType));
  }

  /**
   * Implements {@link Collection#contains} safely for forwarding collections of
   * map entries. If {@code o} is an instance of {@code Map.Entry}, it is
   * wrapped using {@link #unmodifiableEntry} to protect against a possible
   * nefarious equals method.
   *
   * <p>Note that {@code c} is the backing (delegate) collection, rather than
   * the forwarding collection.
   *
   * @param c the delegate (unwrapped) collection of map entries
   * @param o the object that might be contained in {@code c}
   * @return true if {@code c} contains {@code o}
   */
  @SuppressWarnings("unchecked")
  static <K,V> boolean containsEntryImpl(Collection<Entry<K,V>> c, Object o) {
    if (!(o instanceof Entry<?,?>)) {
      return false;
    }
    return c.contains(unmodifiableEntry((Entry<?,?>) o));
  }

  /**
   * Implements {@link Collection#remove} safely for forwarding collections of
   * map entries. If {@code o} is an instance of {@code Map.Entry}, it is
   * wrapped using {@link #unmodifiableEntry} to protect against a possible
   * nefarious equals method.
   *
   * <p>Note that {@code c} is backing (delegate) collection, rather than the
   * forwarding collection.
   *
   * @param c the delegate (unwrapped) collection of map entries
   * @param o the object to remove from {@code c}
   * @return true if {@code c} was changed
   */
  @SuppressWarnings("unchecked")
  static <K,V> boolean removeEntryImpl(Collection<Entry<K,V>> c, Object o) {
    if (!(o instanceof Entry<?,?>)) {
      return false;
    }
    return c.remove(unmodifiableEntry((Entry<?,?>) o));
  }
}
