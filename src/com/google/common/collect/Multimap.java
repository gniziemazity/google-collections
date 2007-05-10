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

import com.google.common.base.Nullable;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * A collection similar to a {@code Map}, but which may associate multiple
 * values with a single key. If you call {@link #put} twice, with the same key
 * but different values, the multimap contains mappings from the key to both
 * values.
 *
 * <p>The methods {@link #get}, {@link #keySet}, {@link #keys}, {@link #values},
 * {@link #entries}, and {@link #asMap} return collections that are views of
 * the multimap. If the multimap is modifable, updating it can change the
 * contents of those collection, and updating those collections will change the
 * multimap. In contrast, {@link #replaceValues} and {@link #removeAll} return
 * collections that are independent of subsequent multimap changes.
 *
 * <p>Depending on the implementation, a multimap may or may not allow duplicate
 * key-value pairs. In other words, the multimap contents after adding the same
 * key and value twice varies between implementations. In multimaps allowing
 * duplicates, the multimap will contain two mappings, and {@code get} will
 * return a collection that includes the value twice. In multimaps not
 * supporting duplicates, the multimap will contain a single mapping from the
 * key to the value, and {@code get} will return a collection that includes the
 * value once.
 *
 * @param <K> the type of keys maintained by this multimap
 * @param <V> the type of mapped values
 * @author jlevy@google.com (Jared Levy)
 */
public interface Multimap<K,V> {

  /** Returns the number of key-value pairs in the multimap. */
  int size();

  /** Returns true if the multimap contains no key-value pairs. */
  boolean isEmpty();

  /**
   * Returns true if the multimap contains any values for the specified key.
   *
   * @param key key to search for in multimap
   */
  boolean containsKey(@Nullable Object key);

  /**
   * Returns true if the multimap contains the specified value for any key.
   *
   * @param value value to search for in multimap
   */
  boolean containsValue(@Nullable Object value);

  /**
   * Returns true if the multimap contains the specified key-value pair.
   *
   * @param key key to search for in multimap
   * @param value value to search for in multimap
   */
  boolean containsEntry(@Nullable Object key, @Nullable Object value);

  /**
   * Returns a collection view of all values associated with a key. If no
   * mappings in the multimap have the provided key, an empty collection is
   * returned.
   *
   * <p>The returned collection may or may not be modifiable, depending on the
   * implementation. If it allows modifications, changes to it will update the
   * underlying multimap.
   *
   * @param key key to search for in multimap
   * @return the collection of values that the key maps to
   */
  Collection<V> get(@Nullable K key);

  /**
   * Stores a key-value pair in the multimap (optional operation).
   *
   * <p>Some multimap implementations allow duplicate key-value pairs, in which
   * case {@code put} always adds a new key-value pair and increases the
   * multimap size by 1. Other implementations prohibit duplicates, and {@code
   * put} will replace an existing key-value pair when the keys and values are
   * equal.
   *
   * @param key key to store in the multimap
   * @param value value to store in the multimap
   * @return true if the method increased the size of the multimap, or
   *    false if the multimap already contained the key-value pair
   *    and doesn't allow duplicates
   * @throws UnsupportedOperationException if not supported
   */
  boolean put(K key, V value);

  /**
   * Stores a collection of values with the same key (optional operation).
   *
   * <p>Some multimap implementations allow duplicate key-value pairs, in which
   * case {@code putAll} adds a new key-value pair for each element in {@code
   * values} and increases the multimap size by the size of {@code values}.
   * Other implementations prohibit duplicates, and {@code putAll} will replace
   * an existing key-value pair when the keys and values are equal.
   *
   * @param key key to store in the multimap
   * @param values values to store in the multimap
   * @throws UnsupportedOperationException if not supported
   */
  void putAll(@Nullable K key, Iterable<? extends V> values);

  /**
   * Copies all of another multimap's key-value pairs into this multimap
   * (optional operation). The order in which the mappings are added is
   * determined by {@code multimap.entries()}.
   *
   * <p>Some multimap implementations allow duplicate key-value pairs, in which
   * case {@code putAll} adds a new key-value pair for each mapping in {@code
   * multimap} and increases the multimap size by the size of the supplied
   * {@code multimap}. Other implementations prohibit duplicates, and {@code
   * putAll} will replace an existing key-value pair when the keys and values
   * are equal.
   *
   * @param multimap mappings to store in this multimap
   * @throws UnsupportedOperationException if not supported
   */
  void putAll(Multimap<? extends K, ? extends V> multimap);

  /**
   * Stores a collection of values with the same key, replacing any existing
   * values for that key (optional operation).
   *
   * @param key key to store in the multimap
   * @param values values to store in the multimap
   * @return the collection of replaced values, or an empty collection if no
   *     values were previously associated with the key. The collection is
   *     modifiable, but updating it will have no effect on the multimap.
   * @throws UnsupportedOperationException if not supported
   */
  Collection<V> replaceValues(@Nullable K key, Iterable<? extends V> values);

  /**
   * Removes a key-value pair from the multimap (optional operation).
   *
   * @param key key of entry to remove from the multimap
   * @param value value of entry to remove the multimap
   * @return true if the multimap changed
   * @throws UnsupportedOperationException if not supported
   */
  boolean remove(@Nullable Object key, @Nullable Object value);

  /**
   * Removes all values associated with a given key (optional operation).
   *
   * @param key key of entries to remove from the multimap
   * @return the collection of removed values, or an empty collection if no
   *     values werer associated with the provided key. The collection is
   *     modifiable, but updating it will have no effect on the multimap.
   * @throws UnsupportedOperationException if not supported
   */
  Collection<V> removeAll(@Nullable Object key);

  /**
   * Removes all key-value pairs from the multimap (optional operation).
   *
   * @throws UnsupportedOperationException if not supported
   */
  void clear();

  /**
   * Returns the set of all keys, each appearing once in the returned set.
   *
   * <p>The returned set may or may not be modifiable, depending on the
   * implementation. If it allows modifications, changes to it will update the
   * underlying multimap.
   *
   * @return collection of distinct keys
   */
  Set<K> keySet();

  /**
   * Returns a collection, which may contain duplicates, of all keys. The number
   * of times of key appears in the returned collection equals the number of
   * mappings the key has in the multimap.
   *
   * <p>The returned multiset may or may not be modifiable, depending on the
   * implementation. If it allows modifications, changes to it will update the
   * underlying multimap.
   *
   * @return a multiset with keys corresponding to the distinct keys of
   *     the multimap and frequencies corresponding to the number of values
   *     that each key maps to
   */
  Multiset<K> keys();

  /**
   * Returns a collection of all values in the multimap.
   *
   * <p>The returned collection may or may not be modifiable, depending on the
   * implementation. If it allows modifications, changes to it will update the
   * underlying multimap.
   *
   * @return collection of values, which may include the same value multiple
   *     times if it occurs in multiple mappings
   */
  Collection<V> values();

  /**
   * Returns a collection of all key-value pairs.
   *
   * <p>The returned collection may or may not be modifiable, depending on the
   * implementation. If it allows modifications, changes to it will update the
   * underlying multimap. The collection supports element removal, which removes
   * the corresponding mapping from the map. It does not support the {@code add}
   * or {@code addAll} operations.
   *
   * @return collection of map entries consisting of key-value pairs
   */
  Collection<Map.Entry<K,V>> entries();

  /**
   * Returns a set of map entries, each of which associates a key with its
   * corresponding values in the multimap.
   *
   * <p>The returned set may or may not be modifiable, depending on the
   * implementation. If it allows modifications, changes to it will update the
   * underlying multimap. The set supports element removal, which removes the
   * corresponding mapping from the map. It does not support the {@code add},
   * {@code addAll} or {@code Entry.setValue} operations.
   *
   * <p>If the multimap is modified while an iteration over the set is in
   * progress (except through the iterator's own {@code remove} operation) the
   * results of the iteration are undefined.
   *
   * @return set of map entries, each mapping a key to a collection of values
   *
   * @deprecated Replaced by {@code asMap().entrySet()}
   */
  @Deprecated Set<Map.Entry<K, Collection<V>>> collectionEntries();

  /**
   * Returns a map view that associates each key with the corresponding values
   * in the multimap.
   *
   * <p>The returned map may or may not be modifiable, depending on the
   * implementation. If it allows modifications, changes to it will update the
   * underlying multimap. In particular, the map may support element removal,
   * which removes the corresponding mappings from the multimap. It never
   * supports {@code setValue()} on the map entries, {@code put}, or {@code
   * putAll}.
   *
   * <p>The collections returned by {@code asMap().get(Object)} have the same
   * behavior as those returned by {@link #get}.
   *
   * @return a map view from a key to its collection of values
   */
  Map<K, Collection<V>> asMap();

  /**
   * Compares the specified object to this multimap for equality. Two multimaps
   * are equal if and only if their map views, as returned by {@link #asMap},
   * are equal.
   *
   * <p>Note that two multimaps with identical key-value mappings may or may not
   * always be equal, depending on the implementation. For example, {@link
   * SetMultimap} equality is independent of ordering, while {@link
   * ListMultimap} equality is dependent on the order of values for a given key.
   *
   * @return true if the object equals this multimap
   * @see Map#equals
   */
  boolean equals(@Nullable Object other);

  /**
   * Returns the hash code for this multimap. The hash code is defined as the
   * hash code of the map view, as returned by {@link Multimap#asMap}.
   *
   * @see Map#hashCode
   */
  int hashCode();
}
